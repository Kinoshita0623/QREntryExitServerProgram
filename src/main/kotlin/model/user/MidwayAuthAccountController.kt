package model.user

import org.mindrot.jbcrypt.BCrypt
import org.sql2o.Sql2o
import java.util.*
import model.*

//data class Token(val token:String)
class MidwayAuthAccountController(sql2o: Sql2o): DBAccountController(sql2o){

    internal data class TokenAndID(val id: String, val token: Token)

    private data class UserData(val id: String, val rawPassword: String)

    companion object {
        private val loginList = Collections.synchronizedList(ArrayList<TokenAndID>())
        private val userData = Collections.synchronizedList(ArrayList<UserData>())
    }


    fun login(id:String, pw:String): Token?{
        //IDとPWをチェック
        val account = super.getAccount(id)?: return null  //無ければNULL
        val b = BCrypt.checkpw(pw,account.password.password)
        userData.add(UserData(id = id, rawPassword = pw))
        return if(b) Token(createToken(id, pw)) else null
    }

    fun logout(token: Token):Boolean{
        val id = getIdForToken(token)?: return false
        removeLoginList(id.id)
        removeUserList(id.id)
        return true
    }

    private fun changeToken(token: Token): String?{

        val tokenId = getIdForToken(token) ?: return null
        val idAndPw = getIdAndPwForId(tokenId.id) ?: return null
        removeLoginList(tokenId.id)
        return createToken(tokenId.id,idAndPw.rawPassword)
        //loginList.add(TokenAndID(id = tokenId.id, token = Token(newToken)))

    }

    private fun createToken(id:String, rawPW: String): String{
        removeLoginList(id)
        removeUserList(id)
        val tokenMaterial =  UUID.randomUUID().toString() + rawPW + id
        val salt = BCrypt.gensalt()

        val token =  BCrypt.hashpw(tokenMaterial,salt)    //トークン作成
        loginList.add(TokenAndID(id, Token(token)))
        return token

    }

    fun checkLoginToken(token : Token): Token?{
        return getIdForToken(token)?.token
    }

    fun isLoginToken(token: Token): Boolean{
        return getIdForToken(token) != null
    }

    internal fun getIdForToken(token: Token): TokenAndID?{
        synchronized(loginList){
            return loginList.first{ idAndToken -> idAndToken.token.token == token.token }
        }
    }

    private fun getIdAndPwForId(id: String): UserData?{
        synchronized(userData){
            return userData.first { it ->
                it.id == id
            }
        }
    }

    private fun removeLoginList(id: String){
        synchronized(loginList){
            val iterator = loginList.iterator()
            while(iterator.hasNext()){
                if(iterator.next().id == id){
                    iterator.remove()
                }
            }
        }

    }

    private fun removeUserList(id: String){
        synchronized(userData){
            val iterator = userData.iterator()
            while(iterator.hasNext()){
                if(iterator.next().id == id){
                    iterator.remove()
                }
            }
        }
    }


}