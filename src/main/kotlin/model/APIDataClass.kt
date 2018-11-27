package model

//Root


//data class CreateAccountStatus(val status:String)
data class Token(val token:String)//TokenをpushするときのAPI
data class ResponseToken(val token:String?, val status: StatusEnumObj)//トークンを返す時のAPI

data class AccountPost(val id:String, val name: String, val password: String)   //アカウントを登録するときのAPI
data class APIStatus(val status:StatusEnumObj, val msg: String)//POSTした時に返ってくるステータスAPI

data class LoginData(val id:String, val password:String)//ログインするときのAPI
//data class Token(val token:String):Serializable
//data class WebAccountInfo(val id:String, val name:String, val token:String, val status:String)//自分のプライベートなアカウント情報

data class GroupAndYourStatus(val groupId:String,val groupName:String, val yourAuthority: String)
data class MyInfo(val id:String, val name:String, val yourGroup: ArrayList<GroupAndYourStatus>)

enum class StatusEnumObj(status: String){
    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    NOT_FOUND("NOT FOUND"),
    INPUT_ERROR("INPUT_ERROR"),
    JSON_ERROR("JSON_ERROR"),
    SERVER_ERROR("SERVER_ERROR"),
    NULL("NULL_POINTER_EXCEPTION")
}

//Group関連
data class CreateGroupInfo(val groupId: String, val groupName: String,  val userToken :String)
data class AddGroupInfo(val groupId:String ,val groupToken:String, val userId:String, val authority:String)
data class GetGroupToken(val groupId:String, val userToken: String)

//入退室管理コード関連
//入退室コード作成リクエスト用
data class ManagementEntryExitCode(val groupId: String, val userToken: String)

//入退室コードレスポンス用
data class EntryExitCode(val groupId: String?, val entryExitCode:String?, val status: StatusEnumObj)

//EntryAndExitLog
//year, month, day, hour, minute, flag
data class EntryExitLogTimeData(val userId:String, val year:Int, val month:Int, val day:Int, val hour:Int, val minute:Int, val flag:String)
data class UserTokenAndGroupId(val userToken: String,val groupId: String)

