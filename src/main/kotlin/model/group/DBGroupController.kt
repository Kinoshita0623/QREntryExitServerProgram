package model.group

import model.user.DBAccountController
//import model.user.UserController
import org.apache.commons.lang3.RandomStringUtils
import org.sql2o.Query
import org.sql2o.Sql2o

abstract class DBGroupController(private val sql2o: Sql2o){

    class InUserController(sql2o: Sql2o): DBAccountController(sql2o){
        fun searchUser(id:String):Boolean{
            return super.getAccount(id) != null
        }
    }
    private val userDBController = InUserController(sql2o)

    protected data class MemberData(val groupId: String, val groupName: String, val token:String, val userId: String, val userName:String, val authority:String)

    private val mapping = HashMap<String,String>()
    init{
        mapping.apply{
            put("PRIMARY_ID", "primary_id")
            put("NAME","name")
            put("TOKEN","TOKEN")
            put("STATUS","status")
            put("ID","id")
            put("AUTHORITY","authority")
            put("GROUPID","groupId")
            put("USERID", "userId")

        }
        sql2o.defaultColumnMappings = mapping
    }

    protected fun createGroup(groupId:String, groupName:String, userId:String):Boolean{

        if(searchGroupAndUser(groupId = groupId)!!.isNotEmpty()){
            return false
        }else if(!userDBController.searchUser(userId)){
            return false
        }
        val token = RandomStringUtils.randomAlphabetic(10)  //Make a Token

        try{
            sql2o.beginTransaction().use{
                val groupQueryText = "INSERT INTO GROUP_TABLE(id,name,token,status) VALUES(:id,:name,:token,:status)"   //INSERT INTO ALLを検討中
                val query = it.createQuery(groupQueryText).apply{
                    addParameter("id",groupId)
                    addParameter("name",groupName)
                    addParameter("token",token)
                    addParameter("status","OK")
                }
                 query.executeUpdate().result

                it.commit()
                return if(addMember(groupId = groupId, userId = userId, authority = AuthorityEnum.ADMIN)){
                    //it.commit()
                    true
                }else{
                    it.rollback()
                    false
                }


            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return false
    }



    /*protected fun addMember(groupId:String, groupToken:String, userId: String, authority: AuthorityEnum, needCheck:Boolean = true):Boolean{

        val doHaveMember = searchGroupAndUser(groupId = groupId, userId = userId)
        val doHaveUser = userDBController.searchUser(userId)
        //val doHaveUser = searchGroupAndUser(userId = userId)
        val doHaveGroup = searchGroupAndUser(groupId = groupId)
        println(doHaveMember)

        if(needCheck){
            when{
                doHaveMember!!.isNotEmpty() ->{
                    println("")
                    return false
                }!doHaveUser ->{
                    return false
                }doHaveGroup!!.isEmpty() ->{
                    return false
                }doHaveGroup[0].token != groupToken -> return false
            }
        }

        //println(doHaveMember)
        println("groupId:$groupId, userId:$userId ,authority:$authority")
        println(doHaveMember)

        val searchUserPrimaryKey = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
        val searchGroupPrimaryKey = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id AND token = :token"
        val memberQueryText = "INSERT INTO MEMBER_TABLE(user_id,group_id,authority) VALUES($searchUserPrimaryKey, $searchGroupPrimaryKey, :authority)"
        try{
            sql2o.beginTransaction().use{
                val memberQuery = it.createQuery(memberQueryText).apply{
                    addParameter("user_id",userId)
                    addParameter("group_id",groupId)
                    addParameter("token",groupToken)
                    addParameter("authority",authority.toString())

                }
                val i = memberQuery.executeUpdate().result
                return if(i  < 1){
                    it.rollback()
                    false
                }else{
                    it.commit()
                    true
                }

            }
        }catch(e: Exception){
            e.printStackTrace()
        }


        return false
    }*/

    protected fun addMember(groupId: String, userId:String,authority: AuthorityEnum):Boolean{
        val groupList = searchGroupAndUser(groupId = groupId, userId = userId)
        return when{

            groupList?.firstOrNull() != null ->{
                println("追加済みのため対象のユーザーをグループに追加することはできませんでした。")
                false
            }
            else ->{
                val searchUserPrimaryId = "SELECT primary_id FROM USER_TABLE WHERE id = :user_id"
                val searchGroupPrimaryId = "SELECT primary_id FROM GROUP_TABLE WHERE id = :group_id"
                val memberQueryText = "INSERT INTO MEMBER_TABLE(user_id,group_id,authority) VALUES($searchUserPrimaryId, $searchGroupPrimaryId, :authority )"
                try{
                    sql2o.beginTransaction().use{
                        val query = it.createQuery(memberQueryText).apply{
                            addParameter("user_id",userId)
                            addParameter("group_id",groupId)
                            addParameter("authority", authority.toString())
                        }
                        val i = query.executeUpdate().result
                        return if(i < 1){
                            it.rollback()
                            println("失敗したああああああああああああああああああああああああああああああああああああああああああああああああああああああああ")
                            false
                        }else{
                            it.commit()
                            println("成功したああああああああああああああああああああああああああああああああああああああああああああああああああああ")
                            true
                        }
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                    println("Exception投げやがったあああああああああああああああああああああああああああああああああああああああああああああああああ")
                    false
                }
            }


        }

    }



    protected fun searchGroupAndUser(groupId:String? = null, userId: String? = null):List<MemberData>?{

        val selectText = "SELECT  GROUP_N.ID AS GROUPID, GROUP_N.NAME AS GROUPNAME, GROUP_N.TOKEN, USER.ID AS USERID, USER.NAME AS USERNAME, MEMBER.AUTHORITY FROM "
        val join1 = "(USER_TABLE USER INNER JOIN MEMBER_TABLE MEMBER ON USER.PRIMARY_ID = MEMBER.USER_ID)"
        val join2 = "INNER JOIN GROUP_TABLE GROUP_N ON MEMBER.GROUP_ID = GROUP_N.PRIMARY_ID "



        fun queryFunc(whereText:String, function:(query:Query)->Query):List<MemberData>?{
            val queryText = selectText + join1 + join2 + whereText
            try{
                sql2o.open().use{
                    val selectQuery = it.createQuery(queryText)
                    function(selectQuery)
                    return selectQuery.executeAndFetch(MemberData::class.java)
                }
            }catch(e:Exception){
                e.printStackTrace()
            }
            return null
        }



        when{
            groupId != null && userId == null ->{
                return queryFunc(" WHERE  GROUP_N.ID = :groupId") {
                    it.addParameter("groupId",groupId)
                }
                //" WHERE  GROUP_N.ID = :groupId"
            }
            groupId == null && userId != null ->{
                return queryFunc(" WHERE USER.ID = :userId"){
                    it.addParameter("userId",userId)
                }
                //" WHERE USER.ID = :userId"
            }
            groupId != null && userId != null ->{
                return queryFunc(" WHERE GROUP_N.ID = :groupId AND USER.ID = :userId"){
                    return@queryFunc it.addParameter("groupId",groupId)
                            .addParameter("userId",userId)
                }
                //" WHERE GROUP_N.ID = :groupId AND USER.ID = :userId"
            }
            else ->{
                throw NullPointerException()
            }
        }

    }
}