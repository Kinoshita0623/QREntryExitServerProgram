package model.user

import model.MyInfo
import model.Token
import model.group.MidwayGroupController

import org.sql2o.Sql2o

class MidwayAccountController(sql2o: Sql2o): DBAccountController(sql2o){
    private val authCon = MidwayAuthAccountController(sql2o)
    private val groupCon = MidwayGroupController(sql2o)

    private fun checkId(id:String):Boolean{
        val idRegex = """[\w]{4,15}""".toRegex()
        return idRegex.matches(id)
    }

    private fun checkName(name: String):Boolean{
        val nameRegex = """[\p{InHiragana}-\p{InKATAKANA}-\p{InCJK_UNIFIED_IDEOGRAPHS}-\w]{2,20}""".toRegex()
        return name.matches(nameRegex)
    }

    private fun checkPassword(password: String):Boolean{
        val pwRegex = """[\w]{4,20}""".toRegex()
        return password.matches(pwRegex)
    }

    private fun checkIdAndNameAndPw(id:String, name:String, password:String):Boolean{
        return checkId(id) && checkName(name) && checkPassword(password)
    }

    fun makeAccount(id:String, name:String, password:String):Boolean{
        return checkIdAndNameAndPw(id,name,password) && super.createAccount(id,name,password) != null
    }

    fun getPrivateAccountInfo(token:Token): MyInfo?{

        try{
            val idAndToken = authCon.getIdForToken(token)
            if(idAndToken != null){
                val data = super.getAccount(idAndToken.id)?: return null
                //return WebAccountInfo(id = data.id, name = data.name, )
                val yourGroupList = groupCon.getYourGroups(token)?: return null
                return MyInfo(id = data.id, name = data.name, yourGroup = yourGroupList)    //グループに所属していない場合MidwayGroupControllerのgetYourGroupsがNULLを返す可能性がある
            }else{
                return null
            }
        }catch(e: Exception){
            return null
        }


        //Tokenと関係性のあるアカウントを発見した場合

    }

    fun searchUser(id: String): String?{
        return super.getAccount(id)?.id
    }
}