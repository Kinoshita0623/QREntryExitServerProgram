package model.entryAndExitCode

import model.group.DBGroupController
import org.sql2o.Sql2o

abstract class DBEntryAndExitCodeController(sql2o: Sql2o): DBGroupController(sql2o){

    //val groupDB = AccessGroupDB(sql2o)
    fun createCode(groupId: String){
        throw Exception("未定義")
    }
}