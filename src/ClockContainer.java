import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.Box;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
  * A panel that contains a clock, timezone and time labels, and a remove button
  */
public class ClockContainer extends JPanel {
	public final GraphicsPanel clock;
	public final ZoneId timezone;

	public final JLabel label_time;
	public final JTextArea label_timezone;
	public final JButton btn_remove;

	static final Font bold = new Font("Helvetica", Font.BOLD, 18);
	static final Font plain = new Font("Helvetica", Font.PLAIN, 18);

	// Used to get the timezone and time easier.
	// Formats to: "(full time zone name)::hour:minute:second AM/PM"
	static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("zzzz::hh:mm:ss a");

	/**
	  * Creates a new container with the given clock and time zone
	  */
	public ClockContainer(GraphicsPanel clock, ZoneId timezone) {
		super();

		this.clock = clock;
		this.timezone = timezone;

		// When this component changes size, resize the clock to fit
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int width = getWidth() - 35;
				int height = getHeight() - 35;
				int size = Math.min(width, height);

				clock.changeSize(size);
				revalidate();
			}
		});

		// The time zone is a text area instead of a label
		// because labels do not support word wrapping
		label_timezone = new JTextArea();
		label_timezone.setFont(bold);
		label_timezone.setEditable(false);
		label_timezone.setLineWrap(true);
		label_timezone.setWrapStyleWord(true);
		label_timezone.setOpaque(false);

		label_time = new JLabel();
		label_time.setFont(plain);

		btn_remove = new JButton("Remove");
		btn_remove.setFont(plain);

		// ---------- LAYOUT ----------

		// GridBagLayout is an alternative grid based layout manager
		// that allows more advanced control of "cell" component layout
		// at the cost taking a little bit more to setup

		setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.left = 5;
		c.insets.bottom = 5;
		c.insets.right = 5;
		c.weightx = 1.0;

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		add(clock, c);

		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.gridy = 1;
		add(label_timezone, c);

		c.gridx = 0;
		c.gridy = 2;
		add(label_time, c);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 3;
		add(btn_remove, c);

		updateUI();
	}

	/**
	  * Updates this container's clock to display the correct time
	  * According to the given time zone and time
	  * @param main the primary time zone and time to adjust for
	  */
	public void updateTime(ZonedDateTime main) {
		ZonedDateTime time = main.withZoneSameInstant(timezone);

		clock.hour = time.getHour();
		clock.minute = time.getMinute();
		clock.second = time.getSecond();

		if (clock.hour == 0)
			clock.hour = 12;
		else if (clock.hour > 12)
			clock.hour %= 12;

		clock.repaint();

		String[] format = formatter.format(time).split("::");
		label_timezone.setText(format[0]);
		label_time.setText(format[1]);
	}
}
