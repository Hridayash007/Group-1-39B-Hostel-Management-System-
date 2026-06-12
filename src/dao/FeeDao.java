package dao;


import database.MYSqlConnector;
import java.sql.*;
import java.util.*;


public class FeeDao {


private MYSqlConnector mysql=
new MYSqlConnector();



public List<Object[]> getPendingFees(int userId){


List<Object[]> list=
new ArrayList<>();


String sql=
"SELECT fee_id,student_name,"
+"room_number,amount,status "
+"FROM fees "
+"WHERE user_id=? "
+"AND status='Pending'";


Connection con=mysql.openConnection();


try(PreparedStatement ps=
con.prepareStatement(sql)){


ps.setInt(1,userId);


ResultSet rs=
ps.executeQuery();


while(rs.next()){


list.add(new Object[]{

rs.getInt("fee_id"),
rs.getString("student_name"),
rs.getString("room_number"),
rs.getDouble("amount"),
rs.getString("status")

});


}


}catch(Exception e){
e.printStackTrace();
}


return list;

}



public boolean markPaid(int feeId){


String sql=
"UPDATE fees SET "
+"status='Paid',paid_date=NOW() "
+"WHERE fee_id=?";


Connection con=mysql.openConnection();


try(PreparedStatement ps=
con.prepareStatement(sql)){


ps.setInt(1,feeId);


return ps.executeUpdate()>0;


}catch(Exception e){
return false;
}

}


}