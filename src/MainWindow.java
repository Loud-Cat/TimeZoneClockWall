import java.awt.Frame;
import java.awt.Font;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;


/**
  * The main window. This is where everything is displayed on the screen.
  * This window is the main GUI the user interacts with.
  */
public class MainWindow extends JFrame {
	private JPanel panel;

	private JLabel label_title, label_mainTime, label_newTime, label_time;
	private JComboBox<String> combo_main, combo_new;
	private JButton btn_mainTime, btn_addTime;

	private TimeDialog timeDialog;
	private ClockWall clockWall;
	private JScrollPane clockScroll;

	private ZoneId mainTimeZone;
	private ZonedDateTime mainTime;

	// Time zone: hour:minute:second AM/PM
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("zzzz: hh:mm:ss a");

	/**
	  * Initializes the window and all of its components.
	  */
	public MainWindow() {
		super("Time Zone Clock Wall");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		label_title = new JLabel("Time Zone Clock Wall");
		label_title.setFont( new Font("Helvetica", Font.BOLD, 26) );

		Font subtitle = new Font("Helvetica", Font.PLAIN, 20);

		label_mainTime = new JLabel("Select main time zone:");
		label_mainTime.setFont(subtitle);

		label_newTime = new JLabel("Select new time zone:");
		label_newTime.setFont(subtitle);

		label_time = new JLabel("Time:");
		label_time.setFont( new Font("Helvetica", Font.BOLD, 16) );

		// Gets a collection of all time zones in their full text format
		// For example, ZoneId "GMT" turns into "Greenwich Mean Time"
		Map<String, String> zones = new HashMap<>();
		for (String id : ZoneId.getAvailableZoneIds())
			zones.put(ZoneId.of(id).getDisplayName(TextStyle.FULL, Locale.getDefault()), id);

		// Sorts the time zones and adds them to a String array
		// to be used as JComboBox selection options
		String[] options = zones.keySet()
			.stream()
			.sorted()
			.toArray(String[]::new);

		combo_main = new JComboBox<String>(options);
		combo_main.setFont(subtitle);

		combo_new = new JComboBox<String>(options);
		combo_new.setFont(subtitle);

		// When the user selects a new MAIN time zone, update existing
		// clocks to display the correct time
		combo_main.addActionListener(evt -> {
			// Unless the user hasn't selected a time, of course
			if (mainTime == null)
				return;

			String id = (String) combo_main.getSelectedItem();
			mainTimeZone = ZoneId.of(id, zones);
			mainTime = mainTime.withZoneSameLocal(mainTimeZone);

			clockWall.setMainTime(mainTime);
			clockWall.updateClocks();

			label_time.setText( formatter.format(mainTime) );
		});

		btn_mainTime = new JButton("Click to select time");
		btn_mainTime.setFont(subtitle);

		btn_addTime = new JButton("Add to Clock Wall");
		btn_addTime.setFont(subtitle);

		// When the user clicks "Click to select time",
		// open the time dialog and prompt them to select a time.
		btn_mainTime.addActionListener(evt -> {
			timeDialog.setVisible(true);
		});

		// When the user clicks "Add to clock wall", add the selected time,
		// with the selected secondary time zone, to the wall of clocks.
		btn_addTime.addActionListener(evt -> {
			String id = (String) combo_new.getSelectedItem();

			if (mainTime == null) {
				JOptionPane.showMessageDialog(
					this,
					"Please select a timezone and time",
					"ERROR",
					JOptionPane.ERROR_MESSAGE
				);
				return;
			}

			// Packing a frame takes some effort, so only do it when you must
			clockWall.setMainTime(mainTime);
			if ( clockWall.addTimeZone(zones.get(id)) ) {
				clockWall.updateClocks();
				pack();
			}
		});

		timeDialog = new TimeDialog(this, "Select a time");
		timeDialog.setResizable(false);

		// When the user clicks "submit", inside the time dialog,
		// set the time and update existing clocks
		timeDialog.btn_submit.addActionListener(evt -> {
			String tz = (String) combo_main.getSelectedItem();

			if (tz == null)
				return;

			mainTimeZone = ZoneId.of(tz, zones);
			mainTime = ZonedDateTime.now(mainTimeZone)
				.with(ChronoField.HOUR_OF_DAY, timeDialog.getHour())
				.with(ChronoField.MINUTE_OF_HOUR, timeDialog.getMinute())
				.with(ChronoField.SECOND_OF_MINUTE, timeDialog.getSecond());

			label_time.setText( formatter.format(mainTime) );

			timeDialog.setVisible(false);
			clockWall.setMainTime(mainTime);
			clockWall.updateClocks();
		});

		// Creates the wall of clocks and adds a vertical-only scrollbar		
		clockWall = new ClockWall();
		clockScroll = new JScrollPane(clockWall);
		clockScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// --------- LAYOUT ----------

		// This panel is used as the main container for the GUI.
		// While it is possible to use the JFrame as the container,
		// this approach is more desirable.
		panel = new JPanel();

		// GroupLayout makes it easier to control the alignment
		// of components in a container.
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// Lays out groups and comonents on their respective axes
		// This might not make sense just by reading the code, as
		// this is usually automatically created by a GUI Builder.
		// If you're interested in understanding what this means,
		// read the docs:
		// https://docs.oracle.com/javase/tutorial/uiswing/layout/group.html

		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(label_title)
				.addGroup(
					layout.createSequentialGroup()
						.addGroup(
							layout.createParallelGroup()
								.addComponent(label_mainTime)
								.addComponent(label_newTime)
						)
						.addGap(5)
						.addGroup(
							layout.createParallelGroup()
								.addComponent(
									combo_main,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE
								)
								.addComponent(
									combo_new,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE
								)
						)
						.addGroup(
							layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(
									btn_mainTime,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.DEFAULT_SIZE,
									250
								)
								.addComponent(
									btn_addTime,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.DEFAULT_SIZE,
									250
								)
						)
				)
				.addComponent(label_time)
				.addComponent(clockScroll)
		);

		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(label_title)
				.addGroup(
					layout.createParallelGroup()
						.addComponent(label_mainTime)
						.addComponent(
									combo_main,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE
						)
						.addComponent(btn_mainTime)
				)
				.addGroup(
					layout.createParallelGroup()
						.addComponent(label_newTime)
						.addComponent(
									combo_new,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE
						)
						.addComponent(btn_addTime)
				)
				.addComponent(label_time)
				.addComponent(clockScroll)
		);

		add(panel);
		pack();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
				MainWindow window = new MainWindow();
				window.setVisible(true);
				window.setExtendedState(window.getExtendedState() | Frame.MAXIMIZED_BOTH);
		});
	}
}
