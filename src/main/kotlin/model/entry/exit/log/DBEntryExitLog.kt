package model.entry.exit.log

import model.EntryExitLogTimeData
import model.EntryExitLogTimeDataAndGroupId
import model.group.DBGroupController
import model.group.MidwayGroupController
import model.user.MidwayAuthAccountController
import org.sql2o.Sql2o
import java.time.LocalDateTime

class DBEntryExitLog(private val sql2o: Sql2o): DBGroupController(sql2o){

    private val mapping = HashMap<String,String>()
    init{
        mapping.apply{
            put("PRIMARY_ID", "primary_id")
            put("NAME","name")
            put("STATUS","status")
            put("ID","id")
            //put("USER_ID","user_id")
            put("AUTHORITY","authority")
            put("GROUPID","groupId")
            put("USERID", "userId")
            //put("TIME","time")
            put("YEAR","year")
            put("MONTH","month")
            put("DAY","day")
            put("HOUR","hour")
            put("MINUTE","minute")
            put("FLAG","flag")
            put("NUMBER","number")
            put("SECOND","second")

        }
        sql2o.defaultColumnMappings = mapping
    }

    private val searchUserPrimaryKey = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
    private val searchGroupPrimaryKey = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id"



    fun addLog(groupId: String, userId:String): Boolean{

        val user = super.searchGroupAndUser(userId = userId, groupId = groupId)
        when{
            user == null ->{
                return false
            }
            user.isEmpty() || user.size > 1 ->{
                return false
            }
        }

        //groupId && userIdは認証済みとする？
        val count= countLog(groupId,userId)
        val w = count % 2 == 0
        val flag = if(w) "入室" else "退室"
        val dateTime = LocalDateTime.now()
        //val searchUserPrimaryKey = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
        //val searchGroupPrimaryKey = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id"
        val logQueryText = "INSERT INTO ENTRY_EXIT_LOG(user_id,group_id,year,month,day,hour,minute,second,flag) VALUES($searchUserPrimaryKey, $searchGroupPrimaryKey, :year, :month, :day, :hour, :minute,:second, :flag)"
        try{
            sql2o.beginTransaction().use{
                val logQuery = it.createQuery(logQueryText).apply{
                    addParameter("user_id",userId)
                    addParameter("group_id",groupId)
                    addParameter("year",dateTime.year)
                    addParameter("month",dateTime.monthValue)
                    addParameter("day",dateTime.dayOfMonth)
                    addParameter("hour",dateTime.hour)
                    addParameter("minute", dateTime.minute)
                    addParameter("second",dateTime.second)
                    addParameter("flag",flag)    //テスト中
                }
                val i = logQuery.executeUpdate().result
                return if(i < 1){
                    it.rollback()
                    false
                }else{
                    it.commit()
                    true
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return false
    }

    fun countLog(groupId: String, userId: String):Int{
        //val searchUserPrimaryKey = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
        //val searchGroupPrimaryKey = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id"
        val countLogQuery = "SELECT COUNT(FLAG) AS NUMBER FROM ENTRY_EXIT_LOG WHERE user_id = $searchUserPrimaryKey AND group_id = $searchGroupPrimaryKey"
        sql2o.open().use{
            val query = it.createQuery(countLogQuery).apply{
                addParameter("user_id",userId)
                addParameter("group_id",groupId)
            }
            val data = query.executeAndFetchFirst(CountLogsData::class.java)
            return data.number
        }
    }
    data class CountLogsData(val number: Int)

    //グループ中の一つのユーザーのログを取得する
    fun getUserInGroupLog(groupId: String, userId: String):List<EntryExitLogTimeData>{
        //val searchUserPrimaryKey = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
        //val searchGroupPrimaryKey = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id"
        val whereQuery = "WHERE user_id = :userId AND group_id = $searchGroupPrimaryKey"
        //val logQuery = "SELECT year, month, day, hour, minute, flag FROM ENTRY_EXIT_LOG $whereQuery"
        val join = "FROM ENTRY_EXIT_LOG LOG INNER JOIN USER_TABLE UT ON LOG.USER_ID = UT.PRIMARY_ID"
        val logQuery = "SELECT UT.id as userId, year, month, day, hour, minute, flag $join $whereQuery"
        sql2o.open().use{
            val query = it.createQuery(logQuery).apply{
                addParameter("userId",userId)
                addParameter("groupId",groupId)
            }
            return query.executeAndFetch(EntryExitLogTimeData::class.java)

        }
    }

    //グループに属する全てのユーザーのログを取得する
    /*fun getAllUsersInGroupLog(groupId: String):List<EntryExitLogTimeData>{
        //UserIdも一緒に必要
        //SELECT ID, YEAR, MONTH, DAY ,HOUR, MINUTE, SECOND, FLAG FROM  ENTRY_EXIT_LOG ELOG INNER JOIN USER_TABLE UT ON ELOG.USER_ID = UT.PRIMARY_ID
        val whereQuery ="WHERE group_id = $searchGroupPrimaryKey"
        val join = "FROM ENTRY_EXIT_LOG LOG INNER JOIN USER_TABLE UT ON LOG.USER_ID = UT.PRIMARY_ID"
        val logQuery = "SELECT id as userId, year, month, day, hour, minute, flag $join $whereQuery"
        sql2o.open().use{
            val query = it.createQuery(logQuery).apply{
                addParameter("group_id",groupId)
            }
            return query.executeAndFetch(EntryExitLogTimeData::class.java)
        }


    }*/

    //グループに属するすべてのユーザーのログを取得する
    fun getAllUsersInGroupAllOrLimitLog(groupId: String, limit: Int = -1):List<EntryExitLogTimeData>{
        val limitCondition = 1

        val limitText = "LIMIT :limit"
        val whereQuery ="WHERE GT.ID = :groupId"
        val join1 = "INNER JOIN USER_TABLE UT ON UT.PRIMARY_ID = LOG.USER_ID"
        val join0 = "FROM (ENTRY_EXIT_LOG LOG INNER JOIN GROUP_TABLE GT ON LOG.GROUP_ID = GT.PRIMARY_ID)"
        val select = "SELECT UT.id as userId, year, month, day, hour, minute, flag"
        val basicQueryText = "$select $join0 $join1 $whereQuery"
        val queryText = if(limit >= limitCondition) "$basicQueryText $limitText" else basicQueryText
        sql2o.open().use{

            return it.createQuery(queryText).apply{
                addParameter("groupId",groupId)
                if(limit >= limitCondition){ addParameter("limit",limit)}
            }.executeAndFetch(EntryExitLogTimeData::class.java)
        }

    }

    //グループに関係なく固有のユーザーにかかわるすべてのログを取得する
    fun getUserAllOrLimitLog(userId: String, limit: Int = -1):List<EntryExitLogTimeDataAndGroupId>{
        val limitCondition = 1  //カウントを開始する条件

        //クエリ文が変化する
        val limitText = " LIMIT :limit"
        val order = "order by year desc,month desc,day desc,hour desc,minute desc,second desc "
        val whereQuery = " WHERE UT.ID = :userId"  // :userId
        val join1 = "INNER JOIN USER_TABLE UT ON UT.PRIMARY_ID = LOG.USER_ID "
        val join0 = "FROM (ENTRY_EXIT_LOG LOG INNER JOIN GROUP_TABLE GT ON LOG.GROUP_ID =GT.PRIMARY_ID) "
        //GroupTableのグループIDの列名はidでややこしいのでgroupIdに変換している
        val select = "SELECT GT.id as groupId, year, month ,day ,hour, minute, flag"
        val queryText =
                if(limit >= limitCondition){
            "$select $join0 $join1 $whereQuery $order $limitText"
        }else{
            "$select $join0 $join1 $whereQuery $order"
        }

        sql2o.open().use{
            val query = it.createQuery(queryText).apply{
                addParameter("userId",userId)
                if(limit >= limitCondition){ addParameter("limit",limit) }
            }
            return query.executeAndFetch(EntryExitLogTimeDataAndGroupId::class.java)
        }



        /*SELECT GT.id as groupId, year, month ,day ,hour, minute, flag FROM (ENTRY_EXIT_LOG LOG INNER JOIN GROUP_TABLE GT ON LOG.GROUP_ID =GT.PRIMARY_ID)
            INNER JOIN USER_TABLE UT ON UT.PRIMARY_ID = LOG.USER_ID WHERE UT.ID = 'TestUser2' order by year desc,month desc,day desc,hour desc,minute desc,second desc LIMIT 3
        * */
    }

}