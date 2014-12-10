package Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import Database.ConnectionPool;

import config.ConfigMng;
import config.db.DatabaseCfg;
import core.Application;
import filestatistics.StatFileState;

public class DatabaseMng {
	
	private static ConnectionPool DataBaseConnect = null;
	private static String driver = "oracle.jdbc.driver.OracleDriver";
	private static int poolMinConnNum = 1;
	private static int poolMaxConnNum = -1;
	private static int poolIncConnNum = 1;
	
	public static void initDatabseMng(){
		
		DatabaseCfg dataBaseCfg = ConfigMng.getInstance().dataBaseCfg;
		
			try {
				DataBaseConnect = new ConnectionPool(
						driver,
						dataBaseCfg.m_strUrl,
						dataBaseCfg.m_strUser,
						dataBaseCfg.m_strPassword,
						poolMinConnNum,
						poolMaxConnNum,
						poolIncConnNum);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Application.m_Logger.info("初始化数据库连接失败");
				e.printStackTrace();
			}
		
	}
	
	/**
	 * 返回一个可用的数据库连接，注意：
	 * 1、申请得到数据库连接conn，必须使用returnLocalDBConnection(Connection)函数将该数据库连接回收
	 * 2、申请得到的数据库连接已经关闭了自动提交
	 * */
	public static Connection GetLocalDBConnection()
			throws  SQLException {
		
		if (DataBaseConnect == null) {
			
			initDatabseMng();
		}
		
		return DataBaseConnect.getConnection();
	}
	
	/**
	 * 回收数据库连接
	 * @param conn
	 */
	public static void returnLocalDBConnection(Connection conn) {
		
		if (DataBaseConnect != null) {
			
			DataBaseConnect.returnConnection(conn);
		}
	}
	
	public static boolean judgeSqlResult(int[] nResults) throws SQLException {
		
		for (int nResult : nResults) {
			
			if (Statement.EXECUTE_FAILED == nResult) {
				
				return false;
			}
		}
		return true;
	}
	
	public static void writeEfileLogToLocalDb(int windFarmID, int type,
			int state, String fileName, Calendar cal, Calendar exeCal,Calendar operateTime) {

		Connection conn = null;
		try {
			conn = DatabaseMng.GetLocalDBConnection();

			Timestamp timestamp = null;
			if (null!=operateTime)
				timestamp = new Timestamp(operateTime.getTimeInMillis());
			writeLogToRunFileState(conn, windFarmID, type, state, fileName, timestamp);
			writeLogToStatFileState(conn, windFarmID, type, state, fileName, cal, exeCal);
		} catch (ClassNotFoundException e) {

			Application.m_Logger.error(e,e);
		} catch (SQLException e) {

			Application.m_Logger.error(e,e);
		} catch (Exception e) {
			
			Application.m_Logger.error(e,e);
		} finally {
			
			Application.ThreadSleep(1000);
			try {
				
				conn.close();
			} catch (SQLException e) {

				Application.m_Logger.error(e,e);
			}
		}
	}
	private static void writeLogToRunFileState(Connection conn, int windFarmID,
			int type, int state, String fileName,Timestamp operateTime) throws SQLException {

		String strInsertSql = "INSERT INTO FD_RUN_FILESTATE"
				+ "(WINDFARMID,TIME,TYPE,STATE,FILENAME,OPERATETIME)" + "VALUES(?,?,?,?,?,?)";
		
		String strInsertSqlNoOperate = "INSERT INTO FD_RUN_FILESTATE"
			+ "(WINDFARMID,TIME,TYPE,STATE,FILENAME)" + "VALUES(?,?,?,?,?)";
	
		Calendar ca = Calendar.getInstance();
		Date date = new Date(ca.getTime().getTime());
		Timestamp timestamps = new Timestamp(date.getTime());

		PreparedStatement InsertSqlStatement = null;
		if (null!=operateTime){
			
			InsertSqlStatement = conn.prepareStatement(strInsertSql);
			InsertSqlStatement.setInt(1, windFarmID);
			InsertSqlStatement.setTimestamp(2, timestamps);
			InsertSqlStatement.setInt(3, type);
			InsertSqlStatement.setInt(4, state);
			InsertSqlStatement.setString(5, fileName);
			InsertSqlStatement.setTimestamp(6, operateTime);
		} else {
			
			InsertSqlStatement = conn.prepareStatement(strInsertSqlNoOperate);
			InsertSqlStatement.setInt(1, windFarmID);
			InsertSqlStatement.setTimestamp(2, timestamps);
			InsertSqlStatement.setInt(3, type);
			InsertSqlStatement.setInt(4, state);
			InsertSqlStatement.setString(5, fileName);
		}
		
		InsertSqlStatement.addBatch();

		if (InsertSqlStatement.executeBatch().length == 1) {
			
			System.out.println("添加" + fileName + "记录成功");
		}
		InsertSqlStatement.close();
		conn.commit();
	}

	private static void writeLogToStatFileState(Connection conn,
			int windFarmID, int type, int state, String fileName, Calendar cal, Calendar executeCal) throws Exception {

		StatFileState.writeRecordToStatFileState(conn, windFarmID, type, state, fileName, cal, executeCal);
	}
}
