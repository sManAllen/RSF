/**
 * 
 */
package ListenUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import Database.DatabaseMng;
import config.ConfigMng;
import config.efile.EFileCfg;
import config.windfarm.WindFarm;
import config.windfarm.WindFarmList;
import core.Application;
import core.powerforecast.ReCreateEfile;
import filestatistics.FileState;
import filestatistics.FileTypeParam;
import filestatistics.FileTypeParamList;
import filestatistics.StatFileStateTable;


/**
 * @author Administrator
 *
 */
public class OperateUserThread extends Thread{
	
	private final SimpleDateFormat m_YMDHMSFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final int CheckTimeInterval = 500;
	private Connection DataBaseConnect = null;
	private FileTypeParamList fileTypeParamList = null ;
	private String strNullChar = "-99";
	Calendar lastUpdateTime = Calendar.getInstance();
	
	public OperateUserThread(){
		
		fileTypeParamList = FileTypeParamList.getInstance();
		
		lastUpdateTime.setTimeInMillis(0);
	}
	
	/**
	 * @function:�����û�����
	 * @param windFarm:��糡��Ϣ
	 * @param DataBaseConnect:���ݿ�����
	 */
	public void ListenUser(WindFarm windFarm, Connection DataBaseConnect) {

		// Calendar ca = Calendar.getInstance();
		Calendar cb = Calendar.getInstance();
		cb.add(Calendar.DAY_OF_MONTH, -1);
		String strSelectSql = "SELECT WINDFARMID,TIME,OPERATE,MESSAGE FROM FD_RUN_USEROPERATE WHERE "
				+ " WINDFARMID=? and TIME>to_date(?,'yyyy-mm-dd HH24:mi:ss')"
				// + " and TIME < to_date(?,'yyyy-mm-dd HH24:mi:ss')"
				+ " AND FLAGS = 1 " + " ORDER BY TIME";

		PreparedStatement SelectSqlStatement = null;
		ResultSet rs = null;

		try {

			SelectSqlStatement = DataBaseConnect.prepareStatement(strSelectSql);

			SelectSqlStatement.setInt(1, windFarm.m_nID);
			SelectSqlStatement
					.setString(2, m_YMDHMSFormat.format(cb.getTime()));
			// SelectSqlStatement
			// .setString(3, m_YMDHMSFormat.format(ca.getTime()));

			rs = SelectSqlStatement.executeQuery();

			while (rs.next()) {
				Timestamp Time = rs.getTimestamp("time");
				int operate = rs.getInt("operate");
				String message = rs.getString("message");

				Calendar calOperate = Calendar.getInstance();
				calOperate.setTime(Time);

				if (operate == 30)  // �û��ֶ��ϱ�������Ԥ��
				{
					Application.m_Logger.info("�����û�����");
					shortPlanCorbyManul(DataBaseConnect, windFarm, message,
							Time, calOperate);
				}else if(operate == 60) // ���¶�ʱ����ʱ��
				{
					// �������⴦�� 
				}
			}

			DataBaseConnect.commit();

		} catch (Exception e) {

			System.out.println(e);
			Application.GetLogger().error(e, e);
		} finally {

			try {

				if (null != SelectSqlStatement)
					SelectSqlStatement.close();
				if (null != rs)
					rs.close();
			} catch (SQLException e) {

				Application.GetLogger().error(e, e);
			}
		}
	}
	
	/**
	 * @function:�����û���ϵͳ�����Ĳ���
	 * @param windFarm:��糡��Ϣ
	 * @param DataBaseConnect:���ݿ�����
	 */
	private void ListenUserForSysParame(WindFarm windFarm, Connection DataBaseConnect) {
		
		String strSelectSql = "SELECT WINDFARMID,TIME,MESSAGE FROM FD_RUN_USEROPERATE WHERE "
				+ " WINDFARMID=? and TIME>to_date(?,'yyyy-mm-dd HH24:mi:ss')"
				+ " AND OPERATE = 60"
				+ " AND FLAGS = 0 " + " ORDER BY TIME DESC";

		PreparedStatement SelectSqlStatement = null;
		ResultSet rs = null;

		try {

			SelectSqlStatement = DataBaseConnect.prepareStatement(strSelectSql);

			SelectSqlStatement.setInt(1, windFarm.m_nID);
			SelectSqlStatement.setString(2, m_YMDHMSFormat.format(lastUpdateTime.getTime()));

			rs = SelectSqlStatement.executeQuery();

			if (rs.next()) {
				Timestamp Time = rs.getTimestamp("time");

				lastUpdateTime.setTime(Time);

				getRatePower(windFarm);     
				
				for(int i=0; i<fileTypeParamList.size(); i++){
					FileTypeParam fileTypeParm = fileTypeParamList.get(i);
					getUpLoadTime(windFarm, fileTypeParm);                   
					refreshStatisticsTime(DataBaseConnect, windFarm, fileTypeParm);  
				}
				
				Application.GetLogger().info(windFarm.m_strFarmName + "ϵͳ�������³ɹ���");
				Application.GetLogger().info("�û�����ʱ��Ϊ��"+ m_YMDHMSFormat.format(lastUpdateTime.getTime()));
			}

			DataBaseConnect.commit();

		} catch (Exception e) {

			System.out.println(e);
			Application.GetLogger().error(e, e);
		} finally {

			try {

				if (null != SelectSqlStatement)
					SelectSqlStatement.close();
				if (null != rs)
					rs.close();
			} catch (SQLException e) {

				Application.GetLogger().error(e, e);
			}
		}
	}
	
	/**
	 * @function ͨ���˹���������
	 * @param DataBaseConnect
	 * @param windFarm
	 * @param message
	 * @param Time
	 * @param calOperate
	 */
	public void shortPlanCorbyManul(Connection DataBaseConnect,
			WindFarm windFarm, String message, Timestamp Time,
			Calendar calOperate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strList[] = message.split("##");
		String fbDateStr = strList[0];

		try {
			Date fbDate = sdf.parse(fbDateStr);
			Calendar ca = Calendar.getInstance();
			ca.setTime(fbDate);
			Application.ZeroHMSM(ca);
			// ca.set(ca.get(Calendar.YEAR), ca.get(Calendar.MONTH),
			// ca.get(Calendar.DAY_OF_YEAR), 0, 15, 0);
			// ca.add(Calendar.DAY_OF_MONTH ,-2);
			fbDate = ca.getTime();
			// if( Application.IsMaster())
			// {
			Application.GetLogger().info("��������" + fbDateStr + "E�ı�");
			//�ֶ����¶���Ԥ��E�ı�
			ReCreateEfile.reCreateDQEfile(windFarm.m_nID, fbDate, calOperate);
			// }
			// ���±�ʶ
			updateFlag(DataBaseConnect, windFarm, Time);
		} catch (Exception e) {
			Application.GetLogger().info(e);
			System.out.println(e);
			Application.GetLogger().info(e);
		}
	}
	
	/**
	 * @function:���±�־λ������־λ����Ϊ 0
	 * @param DataBaseConnect
	 * @param windFarm
	 * @param Time
	 */
	public void updateFlag(Connection DataBaseConnect, WindFarm windFarm,
			Timestamp Time) {
		String updateSql = "UPDATE FD_RUN_USEROPERATE SET FLAGS=0 WHERE WINDFARMID=?"
				+ "and TIME= to_date(?,'yyyy-mm-dd HH24:mi:ss')";
		Date tempDate = new Date(Time.getTime());
		Calendar tempCa = Calendar.getInstance();
		tempCa.setTime(tempDate);
		try {
			PreparedStatement UpdateSqlStatement = DataBaseConnect
					.prepareStatement(updateSql);
			UpdateSqlStatement.setInt(1, windFarm.m_nID);
			UpdateSqlStatement.setString(2, m_YMDHMSFormat.format(tempDate
					.getTime()));
			UpdateSqlStatement.executeUpdate();
			UpdateSqlStatement.close();
		} catch (Exception e) {
			System.out.println(e);
			Application.GetLogger().error(e, e);
		}

	}
	
	public void run(){
		
		WindFarmList farmList = ConfigMng.getInstance().windFarmList;
		
		if (ConnectDb()) {
			for (WindFarm windFarmtemp : farmList) {
				
				getRatePower(windFarmtemp);                
				
				for(int i=0; i<fileTypeParamList.size(); i++){
					FileTypeParam fileTypeParm = fileTypeParamList.get(i);
					getUpLoadTime(windFarmtemp, fileTypeParm);                   
					try {
						refreshStatisticsTime(DataBaseConnect, windFarmtemp, fileTypeParm);
					} catch (Exception e) {
						e.printStackTrace();
					}     
					
				}
			}
		}
		
		while (Application.IsRunning()) {
				
				try {
					
					if (ConnectDb()) {
						
						for (WindFarm windFarm : farmList) {
							
							ListenUser(windFarm, DataBaseConnect);
							
							/* �����û���ϵͳ�������޸�
							������EFileGenerater��PPFSd�����Flags�����޸ģ���˸�Ϊ�����ʱ����в�ѯ�жϣ�*/
							ListenUserForSysParame(windFarm, DataBaseConnect);   //�������⴦��
						}
					}
					DatabaseMng.returnLocalDBConnection(DataBaseConnect);
					
				} catch (Exception e) {
	
					Application.GetLogger().error(e, e);
				} finally {
	
					Application.ThreadSleep(CheckTimeInterval);
				}
			}
	}
	
	/**
	 * ��ȡ��FD_CFG_SYSTEM�е�װ������
	 * @param windFarm
	 */
	private void getRatePower(WindFarm windFarm) {
		
		// ��ѯ�õ���FD_CFG_SYSTEM�е�װ������
		String strSelectSql = "SELECT *"
				+ " FROM FD_CFG_SYSTEM "
				+ " WHERE WINDFARMID = ? " 
				+ " AND NAME = ?";
		PreparedStatement SelectSqlStatement = null;
		ResultSet rs = null;

		try {
			SelectSqlStatement = DataBaseConnect.prepareStatement(strSelectSql);

			SelectSqlStatement.setInt(1, windFarm.m_nID);
			SelectSqlStatement.setString(2, "N8_RATEPOWER");
			rs = SelectSqlStatement.executeQuery();

			if (rs.next()) {
				String strRatePower = rs.getString("VALUE");
				
				if (strRatePower != "" && !strRatePower.equalsIgnoreCase(strNullChar))
					windFarm.m_fCapacity = Float.parseFloat(strRatePower);
			}
		} catch (Exception e) {

			Application.GetLogger().error(e, e);
		} finally {

			try {

				if (null != SelectSqlStatement)
					SelectSqlStatement.close();
				if (null != rs)
					rs.close();
			} catch (SQLException e) {

				Application.GetLogger().error(e, e);
			}
		}
	}
	
	private void setXdTime(EFileCfg eFileCfg, String strTime) {

		if(strTime != "" && !strTime.equalsIgnoreCase(strNullChar)){
			
			eFileCfg.xdEFileCfg.lastCal = timeHMParse(strTime);
		}
	
	}

	private void setQfTime(EFileCfg eFileCfg, String strTime) {

		if(strTime != "" && !strTime.equalsIgnoreCase(strNullChar)){
			
			eFileCfg.qfEFileCfg.lastCal = timeHMParse(strTime);
		}
	
	}

	private void setShort20Time(EFileCfg eFileCfg, String strTime) {

		if(strTime != "" && !strTime.equalsIgnoreCase(strNullChar)){
			
			eFileCfg.shortLoadEFileCfg20.lastCal = timeHMParse(strTime);
		}
	
	}

	private void setShort08Time(EFileCfg eFileCfg, String strTime) {
		
		if(strTime != "" && !strTime.equalsIgnoreCase(strNullChar)){
			
			eFileCfg.shortLoadEFileCfg08.lastCal = timeHMParse(strTime);
		}
	
	}

	public Calendar timeHMParse(String strTime) {

		Calendar cal = null;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {

			Date date = sdf.parse(strTime);
			cal = Calendar.getInstance();
			cal.setTime(date);
		} catch (ParseException e) {

			Application.m_Logger.error(e, e);
		}

		return cal;
	}
	
	private boolean ConnectDb() {
		boolean bOk = true;
		try {
			if (DataBaseConnect == null || DataBaseConnect.isClosed()) {
				DataBaseConnect = DatabaseMng.GetLocalDBConnection();
				DataBaseConnect.setAutoCommit(false);
				Application.m_Logger.info("���������������ݿ�����...");
			}
		} catch (Exception e) {
			bOk = false;
			Application.GetLogger().error(e, e);
		}
		return bOk;
	}
	
	/** ===============================��������ı������޸���Щ���� ===============================*/
	/**
	 * ��ȡ��FD_CFG_SYSTEM�е��ϴ�/���� �Ŀ�ʼʱ��
	 * @param windFarm
	 * @param fileTypeParm
	 */
	private void getUpLoadTime(WindFarm windFarm, FileTypeParam fileTypeParm) {
		
		// ��������Ϊһ�����ϴ�/�����򷵻ء�
		boolean isMultiOneDay = fileTypeParm.multiOneDay;
		if(isMultiOneDay){
			return;
		}
		
		int FileType = fileTypeParm.fileType;
		// ��ѯ�õ���FD_CFG_SYSTEM�е��ϴ�/���� �Ŀ�ʼʱ��
		String strSelectSql = "SELECT *"
				+ " FROM FD_CFG_SYSTEM "
				+ " WHERE WINDFARMID = ? " 
				+ " AND NAME = ?";
		PreparedStatement SelectSqlStatement = null;
		ResultSet rs = null;

		try {
			SelectSqlStatement = DataBaseConnect.prepareStatement(strSelectSql);

			SelectSqlStatement.setInt(1, windFarm.m_nID);
			SelectSqlStatement.setString(2, fileTypeParm.SystemCfgName);
			rs = SelectSqlStatement.executeQuery();

			if (rs.next()) {
				String strValueTime = rs.getString("VALUE");
				
				for(EFileCfg eFileCfg : windFarm.eFileCfgList) {
					
					switch (FileType){
					case FileState.SHORT08_EFILE :
						setShort08Time(eFileCfg,strValueTime);
						break;
						
					case FileState.SHORT20_EFILE :
						setShort20Time(eFileCfg,strValueTime);
						break;
						
					case FileState.QF_EFILE :
						setQfTime(eFileCfg,strValueTime);
						break;
						
					case FileState.XD_EFILE :
						setXdTime(eFileCfg,strValueTime);
						break;
					
					default :
						throw new Exception();
					}
				}
				
			}
		} catch (Exception e) {

			Application.GetLogger().error(e, e);
		} finally {

			try {

				if (null != SelectSqlStatement)
					SelectSqlStatement.close();
				if (null != rs)
					rs.close();
			} catch (SQLException e) {

				Application.GetLogger().error(e, e);
			}
		}
	}

	public void refreshStatisticsTime(Connection conn,
			WindFarm windFarm, FileTypeParam fileTypeParm) throws Exception {
		
		// ��������Ϊһ�����ϴ�/�����򷵻ء�
		boolean isMultiOneDay = fileTypeParm.multiOneDay;
		if(isMultiOneDay){
			return;
		}
		
		if(windFarm.eFileCfgList.size()>0){

			int FileType = fileTypeParm.fileType;
			int nFarmID = windFarm.m_nID;
			EFileCfg eFileCfg = windFarm.eFileCfgList.get(0);
			Calendar cal = Calendar.getInstance();
			
			switch(FileType){
			case FileState.SHORT08_EFILE :
				cal = (Calendar) eFileCfg.shortLoadEFileCfg08.lastCal.clone();
				break;
			case FileState.SHORT20_EFILE :
				cal = (Calendar) eFileCfg.shortLoadEFileCfg20.lastCal.clone();
				break;
			case FileState.QF_EFILE :
				cal = (Calendar) eFileCfg.qfEFileCfg.lastCal.clone();
				break;
			case FileState.XD_EFILE :
				cal = (Calendar) eFileCfg.xdEFileCfg.lastCal.clone();
				break;
			case FileState.UTOMOROWPOWERSCHEDULE_EFILE:
				cal = (Calendar) eFileCfg.PowerScheduleEFileCfg.lastCal.clone();
				break;
			case FileState.UVOLTAGESCHEDULE_EFILE:
				cal = (Calendar) eFileCfg.VoltageScheduleEFileCfg.lastCal.clone();
				break;
			case FileState.LAST_DAY_EFILE:
				cal = (Calendar) eFileCfg.lastDayEFileCfg.lastCal.clone();
				break;
			case FileState.WEATHER20_EFILE:
				cal = (Calendar) eFileCfg.weather20UploadEFileCfg.lastCal.clone();
				break;
			case FileState.WEATHER08_EFILE:
				cal = (Calendar) eFileCfg.weather08UploadEFileCfg.lastCal.clone();
				break;
			case FileState.TREE_DAY_NWP_EFILEe:
				cal = (Calendar) eFileCfg.threeDayNwpEFileCfg.lastCal.clone();
				break;
			default :
				throw new Exception();
			}
			
			StatFileStateTable.refreshFileStateTime(conn, nFarmID, FileType, cal);
			
		}
	}
	/** ===============================��������ı������޸���Щ���� ===============================*/
}
