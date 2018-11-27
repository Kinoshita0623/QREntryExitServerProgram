package model.user

import model.*
import org.mindrot.jbcrypt.BCrypt
import org.sql2o.Sql2o
import java.util.HashMap



abstract class DBAccountController(private val sql2o: Sql2o){
    protected data class LocalDBPrivateAccountInfo(val id: String, val name:String ,val password: String, val salt:String, val status: String)
    //              data class PrivateAccountInfo(val id: String, val name:String, val password:String, val salt:String, val status:String): Serializable


    private val mapping = HashMap<String,String>()
    init{
        mapping.apply{
            put("PRIMARY_ID","primary_id")
            put("ID","id")
            put("NAME","name")
            put("PASSWORD","password")
            put("SALT","salt")
            put("STATUS","status")

        }
        sql2o.defaultColumnMappings = mapping
    }

    protected fun getAccount(id: String): PrivateAccountInfo?{
        try{
            sql2o.open().use{ it ->
                val queryText = "SELECT id,name,password,salt,status FROM USER_TABLE WHERE id = :id"
                val query = it.createQuery(queryText).apply{
                    addParameter("id",id)

                }

                val data = query.executeAndFetchFirst(LocalDBPrivateAccountInfo::class.java)
                if(data != null){
                    return PrivateAccountInfo(
                            id = data.id,
                            name = data.name,
                            password = PasswordAndType(data.password, PasswordType.HASH_PASSWORD),
                            token = TokenAndType(data.salt, TokenType.SALT),
                            status = Status.ACTIVE
                    )
                }

            }
        }catch(e: Exception){
            e.printStackTrace()
        }
        return null
    }

    protected fun createAccount(id:String, name:String, password:String): PrivateAccountInfo? {
        if(getAccount(id) != null){
            return null
        }


        //パスワード生成
        val hashPW = BCrypt.hashpw(password, BCrypt.gensalt())
        val tokenSalt = BCrypt.gensalt()

        try{
            sql2o.beginTransaction().use{
                val queryText = "INSERT INTO USER_TABLE(id,name,password,salt,status)VALUES(:id,:name,:password,:salt,:status)"
                val query = it.createQuery(queryText).apply{
                    addParameter("id",id)
                    addParameter("name",name)
                    addParameter("password",hashPW)
                    addParameter("salt",tokenSalt)  //Tokenの代わりにSaltを保管
                    addParameter("status","ok")

                }
                val i = query.executeUpdate().result

                //println("こちらーデーターベース内よりお送りしますid:$id name:$name password:$hashPW salt:$tokenSalt")
                it.commit()
                //return LocalDBPrivateAccountInfo(id = id, name = name, password = password, salt = tokenSalt, status = "OK")
                return PrivateAccountInfo(id = id, name = name, password = PasswordAndType(password, PasswordType.RAW_PASSWORD), token = TokenAndType(tokenSalt, TokenType.SALT), status = Status.ACTIVE)
            }

        }catch(e: Exception){
            e.printStackTrace()
        }
        return null

    }

    protected fun deleteAccount(id: String): Boolean{
        throw Exception("未実装")
    }

    private fun put(id:String): Boolean {
        return false
    }

    private fun delete(id:String): Boolean {
        return false
    }
}