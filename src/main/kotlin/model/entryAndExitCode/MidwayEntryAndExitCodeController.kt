package model.entryAndExitCode

import model.Token
import model.group.AuthorityEnum
import model.group.AutoAuthorityEnum
import model.group.MidwayGroupController
import model.user.MidwayAccountController
import model.user.MidwayAuthAccountController
import org.apache.commons.lang3.RandomStringUtils
import org.sql2o.Sql2o
import java.util.*

class MidwayEntryAndExitCodeController(sql2o: Sql2o){

    private val authCon = MidwayAuthAccountController(sql2o)
    private val privateAccount = MidwayAccountController(sql2o)
    private val autoAuth = AutoAuthorityEnum()
    private val group = MidwayGroupController(sql2o)

    private data class GroupIdAndCode(val groupId: String, val code:String)
    //private data class GroupAndUserAndAuthority(val groupName: String, val userId: String, val authority: String)

    companion object{
        private val codeList = Collections.synchronizedList(ArrayList<GroupIdAndCode>())
    }



    fun changeXORCreateCode(groupId: String, userToken: Token): String?{
        //対象のユーザーがログインしているか　＆＆　所属しているグループを取得
       val privateInfo = privateAccount.getPrivateAccountInfo(userToken)?: return null//対象のユーザー || 所属しているグループが無い場合はnullを返す

        //取得したグループのリストから指定のグループオブジェクトを取り出す
        val groupYourStatus =privateInfo.yourGroup.first{ it -> it.groupId == groupId }

        val authEnum= autoAuth.getAuthority(groupYourStatus.yourAuthority)

        //権限をチェックADMIN以外許可しない
        if(authEnum == AuthorityEnum.ADMIN){
            //コリジョン対策が必要
            deleteCode(groupId)
            return addCode(groupId)
        }

        return null

    }

    fun searchGroupFromCode(groupCode: String): String?{ //GroupIdが戻ってきます
        synchronized(codeList){
            synchronized(codeList){
                return codeList.firstOrNull{ it -> it.code == groupCode }?.groupId
            }
        }
    }

    fun searchCodeFromGroupId(groupId: String, token: Token): String?{ //Codeが戻ってきます
        val info = authCon.getIdForToken(token)
        if(info == null){
            return null
        }else{
            val groups = group.getYourGroups(token)
            groups?: return null
            val uGroupInfo =groups.firstOrNull{ it -> it.groupId == groupId && it.yourAuthority == AuthorityEnum.ADMIN.toString()}
            if(uGroupInfo == null){
                return null
            }else{
                synchronized(codeList){
                    return codeList.firstOrNull{ it -> it.groupId == groupId }?.code
                }

            }


        }

    } //権限について検討中のため未実装

    private fun addCode(groupId: String): String{
        val code = RandomStringUtils.randomAlphabetic(16)
        codeList.add(GroupIdAndCode(groupId,code))
        return code
    }

    private fun deleteCode(groupId: String){
        synchronized(codeList){
            val iterator = codeList.iterator()
            while(iterator.hasNext()){
                if(iterator.next().groupId == groupId){
                    iterator.remove()
                }
            }
        }
    }
}