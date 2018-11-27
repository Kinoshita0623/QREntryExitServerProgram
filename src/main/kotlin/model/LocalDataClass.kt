package model

import java.io.Serializable

data class PrivateAccountInfo(val id: String, val name:String, val password: PasswordAndType, val token:TokenAndType, val status:Status): Serializable
data class PasswordAndType(val password:String, val type: PasswordType)
data class TokenAndType(val token:String, val type:TokenType)

enum class PasswordType{
    HASH_PASSWORD,RAW_PASSWORD
}

enum class TokenType{
    SALT,TOKEN
}

enum class Status(status:String){
    STOP("STOP"),
    ACTIVE("ACTIVE"),
    PERMANENT_STOP("PERMANENT_STOP")
}