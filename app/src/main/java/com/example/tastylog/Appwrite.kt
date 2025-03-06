package com.example.tastylog

import android.content.Context
import android.util.Log
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.*
import io.appwrite.services.*
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.concurrent.timer
import java.net.URLEncoder

object Appwrite {
    lateinit var client: Client
    lateinit var account: Account
    lateinit var databases: Databases
    lateinit var storage: Storage

    private const val PROJECT_ID = "67c2dd4e00140c0a40cf" // 项目ID
    private const val DATABASE_ID = "67c2dd79003144b9649c" // 数据库ID
    private const val USERS_COLLECTION_ID = "67c2ddda003a261ef14e" // 用户集合ID
    private const val FOOD_LIST_COLLECTION_ID = "67c2dde7002554724586" // 食物列表集合ID
    private const val FOOD_IMAGES_BUCKET_ID = "67c2de08001a22001a6c" // 存储ID

    // 添加自定义协程作用域，替代GlobalScope
    private val appwriteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 添加清理方法
    fun cancelAllRequests() {
        appwriteScope.coroutineContext.cancelChildren()
    }

    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject(PROJECT_ID)

        account = Account(client)
        databases = Databases(client)
        storage = Storage(client)
    }

    //===================================
    // 回调方式 API - 适用于 Java 调用
    //===================================

    // 登录 - 回调版本
    fun loginWithCallback(email: String, password: String, onSuccess: (Session) -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                // 先尝试删除现有会话
                try {
                    account.deleteSession("current")
                } catch (e: Exception) {
                    // 忽略错误
                }

                // 使用正确的邮箱密码登录方法
                val session = account.createEmailPasswordSession(email, password)
                Log.d("Appwrite", "登录成功，用户ID: ${session.userId}")

                withContext(Dispatchers.Main) {
                    onSuccess(session)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "登录失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 注册 - 回调版本
    fun registerWithCallback(email: String, password: String, name: String, onSuccess: (User<Map<String, Any>>) -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                // 1. 创建用户账号
                val userId = ID.unique()
                Log.d("Appwrite", "生成用户ID: $userId")

                // 先尝试删除现有会话
                try {
                    account.deleteSession("current")
                    Log.d("Appwrite", "已删除现有会话")
                } catch (e: Exception) {
                    // 忽略删除会话时的错误，可能本来就没有会话
                    Log.d("Appwrite", "没有现有会话需要删除或删除失败")
                }

                val user = account.create(
                    userId,
                    email,
                    password,
                    name
                )

                // 2. 创建用户文档
                val avatarUrl = "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}"
                val documentId = createUser(email, name, userId, avatarUrl)
                Log.d("Appwrite", "用户文档ID: $documentId")

                // 3. 登录
                try {
                    val session = account.createEmailPasswordSession(email, password)
                    Log.d("Appwrite", "自动登录成功: ${session.userId}")

                    // 4. 创建初始食物列表
                    createInitialFoodListForUser(userId)
                } catch (e: Exception) {
                    Log.e("Appwrite", "自动登录失败: ${e.message}", e)
                }

                // 返回结果
                withContext(Dispatchers.Main) {
                    onSuccess(user)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "注册失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 获取当前用户 - Java 回调版本
    fun getCurrentUserWithCallback(onSuccess: (Map<String, Any>) -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                Log.d("Appwrite", "开始获取当前用户信息")
                val userData = getCurrentUser()
                if (userData != null) {
                    Log.d("Appwrite", "成功获取用户信息，准备回调")
                    Log.d("Appwrite", "完整用户数据: $userData")
                    if (userData.containsKey("avatarUrl")) {
                        Log.d("Appwrite", "头像URL: ${userData["avatarUrl"]}")
                    } else {
                        Log.d("Appwrite", "用户数据中不包含avatarUrl字段")
                    }
                    withContext(Dispatchers.Main) {
                        onSuccess(userData)
                    }
                } else {
                    Log.e("Appwrite", "获取用户信息返回null")
                    withContext(Dispatchers.Main) {
                        onError(Exception("用户信息为空"))
                    }
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "获取用户信息时发生异常: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    //===================================
    // 协程方式 API - 适用于 Kotlin 调用
    //===================================

    // 创建用户文档的独立方法
    private suspend fun createUser(email: String, name: String, userId: String, avatarUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val userData = mapOf(
                    "user_id" to userId,
                    "email" to email,
                    "name" to name,
                    "avatar_url" to avatarUrl
                )
                
                Log.d("Appwrite", "创建用户文档 - 传入数据详情: user_id=$userId, email=$email, name=$name, avatar_url=$avatarUrl")
                
                val document = databases.createDocument(
                    DATABASE_ID,
                    USERS_COLLECTION_ID,
                    ID.unique(),  // 使用唯一ID作为文档ID，而不是userId
                    userData,
                )
                
                Log.d("Appwrite", "用户文档创建成功: 完整数据=${document.data}")
                
                // 尝试列出最近创建的所有文档
                val docs = databases.listDocuments(
                    DATABASE_ID,
                    USERS_COLLECTION_ID,
                    listOf() // 不添加任何过滤条件，获取所有文档
                )
                Log.d("Appwrite", "查询到${docs.documents.size}个文档")
                docs.documents.forEach { 
                    Log.d("Appwrite", "文档ID=${it.id}, 数据=${it.data}")
                }

                document.id
            } catch (e: Exception) {
                Log.e("Appwrite", "创建用户文档失败: ${e.message}", e)
                Log.e("Appwrite", "详细错误: ${e.cause?.message ?: "无详情"}")
                throw e
            }
        }
    }

    // 为用户创建初始的食物列表文档 - 协程版本
    private suspend fun createInitialFoodListForUser(userId: String) {
        try {
            val data = mapOf(
                "food_id" to ID.unique(),
                "user_id" to userId,
                "title" to "默认食物",
                "time" to "2025-03-01",
                "rating" to 0.0,
                "tag" to "#标签",
                "price" to 0.0,
                "img_url" to ""
            )
            
            Log.d("Appwrite", "开始创建初始食物列表，数据: $data")
            
            val document = databases.createDocument(
                DATABASE_ID,  
                FOOD_LIST_COLLECTION_ID,  
                ID.unique(),      
                data               
            )
            
            Log.d("Appwrite", "初始食物列表创建成功: ID=${document.id}, 数据=${document.data}")
        } catch (e: Exception) {
            Log.e("Appwrite", "创建初始食物列表失败: ${e.message}", e)
        }
    }

    // 简化后的注册方法 - 协程版本
    suspend fun register(email: String, password: String, name: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 创建用户账号
                Log.d("Appwrite", "注册开始 - Email: $email, Name: $name")
                
                val user = account.create(
                    ID.unique(), // 使用随机ID
                    email,
                    password,
                    name
                )
                val userId = user.id
                
                Log.d("Appwrite", "用户账号创建成功: ID=$userId")
                
                // 生成头像URL
                val avatarUrl = "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}"
                
                // 2. 创建用户文档
                val documentId = createUser(email, name, userId, avatarUrl)
                Log.d("Appwrite", "用户文档ID: $documentId")
                
                // 3. 登录
                try {
                    val session = account.createEmailPasswordSession(email, password)
                    Log.d("Appwrite", "自动登录成功: ${session.userId}")
                } catch (e: Exception) {
                    Log.e("Appwrite", "自动登录失败: ${e.message}", e)
                    // 登录失败不影响注册结果
                }
                
                userId // 返回用户ID，与TS版本一致
            } catch (e: Exception) {
                Log.e("Appwrite", "注册失败: ${e.message}", e)
                throw e
            }
        }
    }

    // 登录 - 协程版本
    suspend fun login(email: String, password: String): Session {
        return withContext(Dispatchers.IO) {
            try {
                val session = account.createEmailPasswordSession(email, password)
                Log.d("Appwrite", "登录成功: ${session.userId}")
                session
            } catch (e: Exception) {
                Log.e("Appwrite", "登录失败: ${e.message}", e)
                throw e
            }
        }
    }

    // 添加食物项 - 协程版本
    suspend fun addFoodItem(
        userId: String,
        title: String,
        time:String,
        imgUrl: String,
        rating: Double,
        price: Double,
        tag: String
    ): Document<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            val data = mapOf(
                "food_id" to ID.unique(),
                "user_id" to userId,
                "title" to title,
                "time" to time,
                "rating" to rating,
                "price" to price,
                "img_url" to imgUrl,
                "tag" to tag,
            )
            
            Log.d("Appwrite", "开始创建食物项，数据: $data")
            
            val document = databases.createDocument(
                DATABASE_ID,  
                FOOD_LIST_COLLECTION_ID,  
                ID.unique(),      
                data               
            )
            
            Log.d("Appwrite", "食物项创建成功: ID=${document.id}, 数据=${document.data}")
            document
        }
    }

    // 获取用户的所有食物项 - 协程版本
    suspend fun getUserFoodItems(userId: String): List<Document<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = databases.listDocuments(
                    DATABASE_ID,
                    FOOD_LIST_COLLECTION_ID,
                    listOf(
                        io.appwrite.Query.equal("user_id", userId)
                    )
                )
                response.documents
            } catch (e: Exception) {
                Log.e("Appwrite", "获取用户食物列表失败", e)
                emptyList()
            }
        }
    }
    
    // 将 Document 转换为 FoodItem - 辅助方法
    fun documentToFoodItem(document: Document<Map<String, Any>>): com.example.tastylog.model.FoodItem {
        val data = document.data
        
        // 使用位置参数调用 Java 构造函数
        return com.example.tastylog.model.FoodItem(
            data["img_url"] as? String ?: "",          // imageUrl
            data["title"] as? String ?: "",            // title
            data.getOrDefault("time", "") as String,   // time
            (data["rating"] as? Number)?.toFloat() ?: 0f, // rating
            (data["price"] as? Number)?.toString() ?: "0", // price
            listOf(data["tag"] as? String ?: "")       // tag
        )
    }

    // 获取并转换食物列表 - 协程版本
    suspend fun getUserFoodItemsList(userId: String): List<com.example.tastylog.model.FoodItem> {
        val documents = getUserFoodItems(userId)
        return documents.map { documentToFoodItem(it) }
    }

    // 添加更新用户头像的方法 - 协程版本
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.updateDocument(
                    DATABASE_ID,
                    USERS_COLLECTION_ID,
                    userId,
                    mapOf("avatar_url" to avatarUrl)
                )
                true
            } catch (e: Exception) {
                Log.e("Appwrite", "更新用户头像失败: ${e.message}", e)
                false
            }
        }
    }

    // 获取当前用户 - 协程版本
    suspend fun getCurrentUser(): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            try {
                // 获取当前会话用户
                val currentUser = account.get()
                Log.d("Appwrite", "获取到当前用户: ${currentUser?.name}, ID: ${currentUser?.id}")
                
                // 确保用户存在
                if (currentUser?.id != null) {
                    // 查询用户文档以获取更多信息
                    val response = databases.listDocuments(
                        DATABASE_ID,
                        USERS_COLLECTION_ID,
                        listOf(
                            io.appwrite.Query.equal("user_id", currentUser.id)
                        )
                    )
                    
                    Log.d("Appwrite", "查询到用户文档数: ${response.documents.size}")
                    
                    if (response.documents.isNotEmpty()) {
                        val userDoc = response.documents[0]
                        Log.d("Appwrite", "用户文档原始数据: ${userDoc.data}")
                        
                        val userData = userDoc.data.toMutableMap()
                        
                        // 添加从账户中获取的其他字段
                        userData["name"] = currentUser.name
                        userData["email"] = currentUser.email
                        
                        // 确保头像URL正确，检查原始字段存在
                        val avatarUrl = userData["avatar_url"] as? String
                        Log.d("Appwrite", "原始头像URL: $avatarUrl")

                        // 如果原始URL为空，生成一个更安全的头像URL
                        if (avatarUrl.isNullOrEmpty()) {
                            val encodedName = URLEncoder.encode(currentUser.name, "UTF-8")
                            // 使用更多参数，指定格式为.png
                            val defaultAvatarUrl = "https://ui-avatars.com/api/?name=$encodedName&size=200&background=random&format=png&rounded=true"
                            userData["avatarUrl"] = defaultAvatarUrl
                            Log.d("Appwrite", "生成新的头像URL: $defaultAvatarUrl")
                        } else {
                            userData["avatarUrl"] = avatarUrl
                        }
                        
                        Log.d("Appwrite", "最终处理后的数据: name=${userData["name"]}, avatarUrl=${userData["avatarUrl"]}")
                        return@withContext userData
                    } else {
                        // 找不到用户文档时，生成默认头像并返回基本信息
                        val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${currentUser.name.replace(" ", "+")}"
                        val basicData = mapOf(
                            "name" to currentUser.name,
                            "email" to currentUser.email,
                            "avatarUrl" to defaultAvatarUrl
                        )
                        Log.d("Appwrite", "未找到用户文档，使用默认头像: $defaultAvatarUrl")
                        return@withContext basicData
                    }
                }
                
                // 没有当前用户时返回 null
                Log.d("Appwrite", "没有当前登录用户")
                null
            } catch (e: Exception) {
                Log.e("Appwrite", "获取当前用户失败: ${e.message}", e)
                null
            }
        }
    }

    // 登出 - 回调版本
    fun logoutWithCallback(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                Log.d("Appwrite", "开始登出")
                account.deleteSession("current")
                Log.d("Appwrite", "登出成功")
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "登出失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 登出 - 协程版本
    suspend fun logout(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                account.deleteSession("current")
                true
            } catch (e: Exception) {
                Log.e("Appwrite", "登出失败: ${e.message}", e)
                false
            }
        }
    }

    fun getDatabaseId() = DATABASE_ID
    fun getUsersCollectionId() = USERS_COLLECTION_ID
}