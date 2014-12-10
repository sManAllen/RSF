


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


public class UiLogAppender extends AppenderSkeleton {


	protected void append(LoggingEvent event) {

		MainWnd.GetTableModel().addEvent(new EventDetails(event));
	}


	public void close() {

		
	}


	public boolean requiresLayout() {

		return false;
	}

}
