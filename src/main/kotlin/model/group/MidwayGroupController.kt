package model.group
import model.*
import model.user.MidwayAuthAccountController

import org.sql2o.Sql2o

class MidwayGroupController(sql2o: Sql2o): DBGroupController(sql2o){
    private val account = MidwayAuthAccountController(sql2o)

    fun createGroup(token: Token, groupId:String, groupName:String): Boolean{
        val accountInfo = account.getIdForToken(token)
        val id = accountInfo?.id
        return if(id == null){    //Tokenが存在しない（ログインしていない,Tokenの値が不正である）
            false
        }else{
            val trimGroupId = groupId.trim()
            super.createGroup(groupId = trimGroupId, groupName = groupName, userId = id)
        }
    }

    /*fun addMember(groupId: String, groupToken: String, userId: String, authority: AuthorityEnum): Boolean{
        return super.addMember(groupId = groupId, groupToken = groupToken, userId = userId, authority = authority, needCheck = true)
    }*/
    fun addMember(groupId: String, addUserId: String, authority: AuthorityEnum, token: Token):Boolean{
        val tokenAndId = account.getIdForToken(token)
        val id = tokenAndId?.id
        if(id == null){
            return false
        }else{
            val groupList =super.searchGroupAndUser(groupId = groupId, userId = id)

            groupList?: return false
            val userGroupData = groupList.firstOrNull{it -> it.groupId == groupId }
            userGroupData?: return false

            return if(AutoAuthorityEnum().getAuthority(userGroupData.authority) == AuthorityEnum.ADMIN){
                super.addMember(groupId = groupId ,userId = addUserId, authority = authority)
            }else{
                false
            }
        }
    }

    fun getYourGroups(token: Token):ArrayList<GroupAndYourStatus>?{
        val tokenAndId = account.getIdForToken(token)
        val id = tokenAndId?.id
        if(id == null){
            return null
        }else{
            val memberList = super.searchGroupAndUser(groupId = null, userId = id)?: return null
            val yourGroupList = ArrayList<GroupAndYourStatus>()
            memberList.map{ it -> GroupAndYourStatus(groupId = it.groupId, groupName = it.groupName, yourAuthority = it.authority)}
                    .forEach{ it -> yourGroupList.add(it) }
            return yourGroupList
        }
    }

    /*fun getGroupToken(token: Token, groupId: String):String?{
        val tokenAndId = account.getIdForToken(token)
        val id = tokenAndId?.id?: return null
        val dataList = super.searchGroupAndUser(groupId = groupId, userId = id)?: return null
        val data = dataList.firstOrNull()
        if(data != null && data.authority == "ADMIN"){
            return data.token
        }
        return null

    }*/

    fun groupGetUserAuthority(userId: String, groupId: String):AuthorityEnum?{
        val userData = super.searchGroupAndUser(groupId = groupId, userId = userId)?.firstOrNull()
        return if(userData == null){
            null
        }else{
           AutoAuthorityEnum().getAuthority(userData.authority)
        }

    }

    /*fun getGroupUser(groupId: String, token: Token): String?{
        val tokenAndId = account.getIdForToken(token)
        if(tokenAndId == null){
            return null
        }else{
            val memberList = super.searchGroupAndUser(groupId = groupId, userId = null)

        }
    }*/

}