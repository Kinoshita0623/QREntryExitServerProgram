package model.group

enum class AuthorityEnum(authority:String){
    ADMIN("ADMIN"),
    NORMAL("NORMAL"),
    LOW("LOW")
}

class AutoAuthorityEnum{
    fun getAuthority(text: String): AuthorityEnum {
        return when(text){
            "ADMIN" ->{
                AuthorityEnum.ADMIN
            }
            "NORMAL" ->{
                AuthorityEnum.NORMAL
            }
            "LOW" ->{
                AuthorityEnum.LOW
            }else ->{
                AuthorityEnum.NORMAL
            }
        }
    }
}