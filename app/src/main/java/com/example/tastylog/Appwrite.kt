package com.example.tastylog

import android.content.Context
import android.util.Log
import com.example.tastylog.config.AppConfig
import com.example.tastylog.model.FoodItem
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import io.appwrite.models.InputFile
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URLEncoder

/**
 * Appwrite 服务封装类
 * 负责与Appwrite后端服务交互，包括用户认证、数据读写和文件存储等功能
 */
object Appwrite {
    lateinit var client: Client
    lateinit var account: Account
    lateinit var databases: Databases
    lateinit var storage: Storage

    // 使用自定义协程作用域替代GlobalScope
    private val appwriteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 取消所有正在进行的网络请求
     */
    fun cancelAllRequests() {
        appwriteScope.coroutineContext.cancelChildren()
    }

    /**
     * 初始化Appwrite客户端
     * @param context 应用上下文
     */
    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject(AppConfig.PROJECT_ID)

        account = Account(client)
        databases = Databases(client)
        storage = Storage(client)
    }

    //===================================
    // 回调方式 API - 适用于 Java 调用
    //===================================

    /**
     * 用户登录
     * @param email 用户邮箱
     * @param password 用户密码
     * @param onSuccess 登录成功回调
     * @param onError 登录失败回调
     */
    fun loginWithCallback(email: String, password: String, onSuccess: (Session) -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                // 清除现有会话
                try {
                    account.deleteSession("current")
                } catch (e: Exception) {
                    // 忽略错误，可能没有现有会话
                }

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
                    AppConfig.DATABASE_ID,
                    AppConfig.USERS_COLLECTION_ID,
                    ID.unique(),  // 使用唯一ID作为文档ID，而不是userId
                    userData,
                )
                
                Log.d("Appwrite", "用户文档创建成功: 完整数据=${document.data}")
                
                // 尝试列出最近创建的所有文档
                val docs = databases.listDocuments(
                    AppConfig.DATABASE_ID,
                    AppConfig.USERS_COLLECTION_ID,
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
                AppConfig.DATABASE_ID,  
                AppConfig.FOOD_LIST_COLLECTION_ID,  
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
                AppConfig.DATABASE_ID,  
                AppConfig.FOOD_LIST_COLLECTION_ID,  
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
                    AppConfig.DATABASE_ID,
                    AppConfig.FOOD_LIST_COLLECTION_ID,
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
    fun documentToFoodItem(document: Document<Map<String, Any>>): FoodItem {
        val foodItem = FoodItem()
        
        // 设置文档ID (这是关键)
        foodItem.setDocumentId(document.id)
        
        // 设置其他属性
        foodItem.setId(document.data["food_id"] as? String ?: "")
        foodItem.setTitle(document.data["title"] as? String ?: "")
        foodItem.setTime(document.data["time"] as? String ?: "")
        foodItem.setRating((document.data["rating"] as? Double)?.toFloat() ?: 0f)
        
        // 处理价格 - 数据库中是Double类型，转换为String
        val priceValue = document.data["price"] as? Double
        foodItem.setPrice(if (priceValue != null) "¥$priceValue" else "")
        
        // 处理标签 - 数据库中是单个tag字段，而不是tags列表
        val tag = document.data["tag"] as? String
        if (!tag.isNullOrEmpty()) {
            val tags = ArrayList<String>()
            tags.add(tag)
            foodItem.setTags(tags) // 使用setter方法
        }
        
        // 处理图片URL
        foodItem.setImageUrl(document.data["img_url"] as? String ?: "")
        
        return foodItem
    }

    // 获取并转换食物列表 - 协程版本
    suspend fun getUserFoodItemsList(userId: String): List<FoodItem> {
        val documents = getUserFoodItems(userId)
        return documents.map { documentToFoodItem(it) }
    }

    // 添加更新用户头像的方法 - 协程版本
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                databases.updateDocument(
                    AppConfig.DATABASE_ID,
                    AppConfig.USERS_COLLECTION_ID,
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
                Log.d("Appwrite", "获取到当前用户: ${currentUser.name}, ID: ${currentUser.id}")
                
                // 确保用户存在
                if (currentUser.id != null) {
                    // 查询用户文档以获取更多信息
                    val response = databases.listDocuments(
                        AppConfig.DATABASE_ID,
                        AppConfig.USERS_COLLECTION_ID,
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

    fun getDatabaseId() = AppConfig.DATABASE_ID
    fun getUsersCollectionId() = AppConfig.USERS_COLLECTION_ID

    // 添加获取当前用户ID的方法，供Java代码调用
    fun getCurrentUserId(): String {
        var userId = ""
        runBlocking {
            try {
                val session = account.get()
                userId = session.id
            } catch (e: Exception) {
                Log.e("Appwrite", "获取当前用户ID失败: ${e.message}", e)
            }
        }
        return userId
    }

    // 添加上传文件的回调方法，供Java代码调用
    fun uploadFileWithCallback(
        bucketId: String,
        fileName: String,
        fileBytes: ByteArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        appwriteScope.launch {
            try {
                // 添加MIME类型检测
                val mimeType = getMimeType(fileName)
                
                val file = storage.createFile(
                    bucketId,
                    ID.unique(),
                    InputFile.fromBytes(
                        fileBytes,
                        fileName,
                        mimeType // 使用检测到的MIME类型
                    )
                )
                
                withContext(Dispatchers.Main) {
                    onSuccess(file.id)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "上传文件失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 添加一个辅助方法来根据文件名确定MIME类型
    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", ignoreCase = true) || 
            fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            fileName.endsWith(".bmp", ignoreCase = true) -> "image/bmp"
            // 默认使用通用二进制类型
            else -> "application/octet-stream"
        }
    }

    // 修改获取文件预览URL的方法
    fun getFilePreviewUrl(bucketId: String, fileId: String): String {
        // 直接构建URL
        return "${client.endpoint}/storage/buckets/$bucketId/files/$fileId/view?project=${AppConfig.PROJECT_ID}"
    }

    // 获取用户美食列表时确保返回包含正确的文档ID
    fun getUserFoodItemsWithCallback(
        userId: String,
        onSuccess: (List<Document<Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        appwriteScope.launch {
            try {
                val documents = databases.listDocuments(
                    AppConfig.DATABASE_ID,
                    AppConfig.FOOD_LIST_COLLECTION_ID,
                    listOf(
                        io.appwrite.Query.equal("user_id", userId)
                    )
                )
                
                // 添加日志
                documents.documents.forEach { doc ->
                    Log.d("Appwrite", "获取到文档: ID=${doc.id}, 标题=${doc.data["title"]}")
                }
                
                withContext(Dispatchers.Main) {
                    onSuccess(documents.documents)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "获取美食列表失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 修改创建食物记录的回调方法，添加location参数
    fun addFoodItemWithCallback(
        userId: String,
        title: String,
        time: String,
        imgUrl: String,
        rating: Double,
        price: Double,
        tag: String,
        content: String,
        location: String,  // 新增location参数
        onSuccess: (Document<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        appwriteScope.launch {
            try {
                val data = mapOf(
                    "food_id" to ID.unique(),
                    "user_id" to userId,
                    "title" to title,
                    "time" to time,
                    "rating" to rating,
                    "price" to price,
                    "img_url" to imgUrl,
                    "tag" to tag,
                    "content" to content,
                    "location" to location 
                )
                
                val document = databases.createDocument(
                    AppConfig.DATABASE_ID,
                    AppConfig.FOOD_LIST_COLLECTION_ID,
                    ID.unique(),
                    data
                )
                
                withContext(Dispatchers.Main) {
                    onSuccess(document)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "创建食物记录失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 修改删除食物记录的方法
    fun deleteFoodItemWithCallback(
        foodId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        appwriteScope.launch {
            try {
                Log.d("Appwrite", "开始删除文档, ID=$foodId, 数据库=${AppConfig.DATABASE_ID}, 集合=${AppConfig.FOOD_LIST_COLLECTION_ID}")
                
                // 删除文档
                databases.deleteDocument(
                    AppConfig.DATABASE_ID,
                    AppConfig.FOOD_LIST_COLLECTION_ID,
                    foodId
                )
                
                Log.d("Appwrite", "删除文档成功, ID=$foodId")
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "删除食物记录失败: ID=$foodId, 错误=${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 更新食物记录
    fun updateFoodItemWithCallback(
        userId: String,
        documentId: String,
        title: String,
        time: String,
        imgUrl: String,
        rating: Double,
        price: Double,
        tag: String,
        content: String,
        location: String,
        onSuccess: (Document<Map<String, Any>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        appwriteScope.launch {
            try {
                val data = mapOf(
                    "user_id" to userId,
                    "title" to title,
                    "time" to time,
                    "img_url" to imgUrl,
                    "rating" to rating,
                    "price" to price,
                    "tag" to tag,
                    "content" to content,
                    "location" to location
                )
                
                val document = databases.updateDocument(
                    AppConfig.DATABASE_ID,
                    AppConfig.FOOD_LIST_COLLECTION_ID,
                    documentId,
                    data
                )
                
                withContext(Dispatchers.Main) {
                    onSuccess(document)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "更新食物记录失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 修改更新用户名方法，先查询用户文档
    fun updateUserName(newName: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                // 获取当前用户ID
                val userId = account.get().id
                Log.d("Appwrite", "准备更新用户名，用户ID: $userId")
                
                // 先查询用户文档
                val response = databases.listDocuments(
                    AppConfig.DATABASE_ID,
                    AppConfig.USERS_COLLECTION_ID,
                    listOf(
                        Query.equal("user_id", userId)
                    )
                )
                
                if (response.documents.isEmpty()) {
                    Log.d("Appwrite", "未找到用户文档，创建新文档")
                    // 创建新文档
                    databases.createDocument(
                        AppConfig.DATABASE_ID,
                        AppConfig.USERS_COLLECTION_ID,
                        ID.unique(),
                        mapOf(
                            "user_id" to userId,
                            "name" to newName,
                            "avatar_url" to ""
                        )
                    )
                } else {
                    // 获取文档ID并更新
                    val documentId = response.documents[0].id
                    Log.d("Appwrite", "找到用户文档ID: $documentId")
                    
                    databases.updateDocument(
                        AppConfig.DATABASE_ID,
                        AppConfig.USERS_COLLECTION_ID,
                        documentId,
                        mapOf("name" to newName)
                    )
                }
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "更新用户名失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 修改更新头像方法，先查询用户文档
    fun updateUserAvatar(avatarUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                val userId = account.get().id
                Log.d("Appwrite", "准备更新用户头像，用户ID: $userId")
                
                // 先查询用户文档
                val response = databases.listDocuments(
                    AppConfig.DATABASE_ID,
                    AppConfig.USERS_COLLECTION_ID,
                    listOf(
                        Query.equal("user_id", userId)
                    )
                )
                
                Log.d("Appwrite", "查询用户文档结果: 找到 ${response.documents.size} 个文档")
                response.documents.forEach { doc ->
                    Log.d("Appwrite", "文档信息: id=${doc.id}, data=${doc.data}")
                }
                
                if (response.documents.isEmpty()) {
                    Log.d("Appwrite", "未找到用户文档，创建新文档")
                    val newDoc = databases.createDocument(
                        AppConfig.DATABASE_ID,
                        AppConfig.USERS_COLLECTION_ID,
                        ID.unique(),
                        mapOf(
                            "user_id" to userId,
                            "name" to account.get().name,
                            "avatar_url" to avatarUrl
                        )
                    )
                    Log.d("Appwrite", "创建新文档成功: id=${newDoc.id}")
                } else {
                    val documentId = response.documents[0].id
                    Log.d("Appwrite", "找到用户文档ID: $documentId")
                    
                    val updatedDoc = databases.updateDocument(
                        AppConfig.DATABASE_ID,
                        AppConfig.USERS_COLLECTION_ID,
                        documentId,
                        mapOf("avatar_url" to avatarUrl)
                    )
                    Log.d("Appwrite", "更新文档成功: id=${updatedDoc.id}")
                }
                
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "更新用户头像失败: ${e.message}", e)
                Log.e("Appwrite", "详细错误: ", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    // 修改上传头像方法
    fun uploadAvatar(fileName: String, fileBytes: ByteArray, 
                    onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        appwriteScope.launch {
            try {
                Log.d("Appwrite", "开始上传头像: fileName=$fileName, fileSize=${fileBytes.size}")
                
                // 确保文件名有正确的扩展名
                val finalFileName = if (!fileName.lowercase().endsWith(".jpg") 
                    && !fileName.lowercase().endsWith(".jpeg")) {
                    "$fileName.jpg"
                } else {
                    fileName
                }
                
                // 检查文件大小
                if (fileBytes.size > 5 * 1024 * 1024) { // 5MB限制
                    throw Exception("文件太大，请选择较小的图片")
                }
                
                // 创建带有MIME类型的InputFile
                val inputFile = InputFile.fromBytes(
                    bytes = fileBytes,
                    filename = finalFileName,
                    mimeType = "image/jpeg"  // 显式指定MIME类型
                )
                
                Log.d("Appwrite", "准备上传文件: filename=$finalFileName")
                
                // 上传文件
                val result = storage.createFile(
                    bucketId = AppConfig.FOOD_IMAGES_BUCKET_ID,
                    fileId = ID.unique(),
                    file = inputFile
                )
                
                Log.d("Appwrite", "文件上传成功: fileId=${result.id}")
                
                // 生成文件URL
                val fileUrl = getFilePreviewUrl(
                    AppConfig.FOOD_IMAGES_BUCKET_ID,
                    result.id
                )
                
                Log.d("Appwrite", "生成文件访问URL: $fileUrl")
                
                withContext(Dispatchers.Main) {
                    onSuccess(fileUrl)
                }
            } catch (e: Exception) {
                Log.e("Appwrite", "上传头像失败: ${e.message}", e)
                Log.e("Appwrite", "详细错误: ", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}