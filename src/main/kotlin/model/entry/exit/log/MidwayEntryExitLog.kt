package model.entry.exit.log

import model.EntryExitLogTimeData
import model.Token
import model.entryAndExitCode.MidwayEntryAndExitCodeController
import model.group.AuthorityEnum
import model.group.MidwayGroupController
import model.user.MidwayAuthAccountController
import org.sql2o.Sql2o

class MidwayEntryExitLog(sql2o: Sql2o){
    private val dbLog = DBEntryExitLog(sql2o)
    private val codeController = MidwayEntryAndExitCodeController(sql2o)
    private val midGroup = MidwayGroupController(sql2o)
    private val authUser = MidwayAuthAccountController(sql2o)

    fun addLog(userId: String, groupCode: String): Boolean{
        val groupId =codeController.searchGroupFromCode(groupCode)
        return if(groupId == null){
            false
        }else{
            dbLog.addLog(userId = userId, groupId = groupId)
        }
    }

    fun getUserAllLog(token: Token, groupId: String):List<EntryExitLogTimeData>?{
        val tokenAndId = authUser.getIdForToken(token)
        tokenAndId?: return null
        return dbLog.getUsersAllEntryExitLog(groupId = groupId, userId = tokenAndId.id)
    }

    fun getGroupAllUserAllLog(token: Token, groupId: String):List<EntryExitLogTimeData>?{
        val tokenAndId = authUser.getIdForToken(token)
        tokenAndId?: return null
        val authority = midGroup.groupGetUserAuthority(userId = tokenAndId.id, groupId = groupId)
        if(authority == AuthorityEnum.ADMIN){
            return dbLog.getGroupAllUserAllEntryExitLog(groupId)
        }
        return null
    }
}