import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;

/**
  * A container for virtual analog clocks on a wall.
  */
public class ClockWall extends JPanel {
	private ZonedDateTime mainTime;
	private Map<String, ClockContainer> containers;

	/**
	  * Initializes the wall and all of its components
	  */
	public ClockWall() {
		super();

		// The containers for each time zone clock
		containers = new HashMap<>();

		setBackground( Color.LIGHT_GRAY );
		setLayout( new GridLayout(0, 4, 5, 5) );
	}

	/**
	  * Sets the "main" time zone and updates clocks accordingly.
	  * @param mainTime the "main" time zone
	  */
	public void setMainTime(ZonedDateTime mainTime) {
		this.mainTime = mainTime;

		updateClocks();
	}

	/**
	  * Adds a new clock with the given time zone to the wall, unless
	  * the clock already exists on the wall.
	  * @param id the ID of the new time zone
	  * @return true if the number of clocks changes, false otherwise
	  */
	public boolean addTimeZone(String id) {
		if ( containers.containsKey(id) )
			return false;

		ZoneId tz = ZoneId.of(id);
		GraphicsPanel clock = new GraphicsPanel();
		clock.changeSize(200);

		ClockContainer cc = new ClockContainer(clock, tz);
		containers.put(id, cc);

		// When the user clicks "remove", remove this clock
		cc.btn_remove.addActionListener(evt -> {
			remove(cc);
			revalidate();
			updateUI();

			containers.remove(id);

			// Get the main window
			MainWindow mw = (MainWindow) SwingUtilities.getAncestorOfClass​(
				MainWindow.class, this);

			// Pack and update the main window to see the clock removed
			mw.pack();
			mw.revalidate();
		});

		add(cc);
		revalidate();
		updateUI();

		return true;
	}

	/**
	  * Updates all clocks inside this wall
	  */
	public void updateClocks() {
		for (ClockContainer cc : containers.values())
			cc.updateTime(mainTime);
	}
}
