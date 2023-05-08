import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Point;

import java.awt.event.MouseEvent;

/**
  * A digital analog clock, capable of drawing any 12-hour time.
  * The clock's time is controlled through its instance variables:
  * hour, minute, and second. However, the repaint() method must be called
  * after you change the time in order to see the change.
  */
public class GraphicsPanel extends JPanel {
	int hour, minute, second;

	// These fields represent possible selected hands on the clock
	static final int NONE = -1;
	static final int HOUR = 0;
	static final int MINUTE = 1;
	static final int SECOND = 2;

	int selected = NONE;

	int canvasWidth = 500, canvasHeight = 500;
	int rad = canvasWidth * 3/8;

	static final double TWO_PI = 2 * Math.PI;

	/**
	  * Initializes the clock with default values
	  */
	public GraphicsPanel() {
		super();

		setPreferredSize( new Dimension(canvasWidth, canvasHeight) );
		setBackground(Color.WHITE);
		reset();
		repaint();
	}

	/**
	  * Resets the clock to 01:15:00.
	  * By spreading the clock hands away from each other, it will be easier
	  * for the user to select a hand.
	*/
	public void reset() {
		hour = 1;
		minute = 15;
		second = 0;
	}

	/**
	  * Changes the size of the clock to the selected width.
	  * Also resizes the radius of the clock accordingly (3/8ths the width)
	  * @param width the new width and height of the clock
	  */
	void changeSize(int width) {
		canvasWidth = canvasHeight = width;
		rad = width * 3/8;

		setPreferredSize( new Dimension(width, width) );
	}

	/**
	  * Optional mouse press event handler that changes the selected hand.
	  * @param e the mouse event to handle
	  */
	public void mousePressed(MouseEvent e) {
		Point mouse = e.getPoint();
		Point hp = getHourPoint();
		Point mp = getMinutePoint();
		Point sp = getSecondPoint();

		// origin, angle
		double ox = canvasWidth/2, oy = canvasHeight/2;
		double t = Math.atan2(mouse.y - oy, mouse.x - ox);

		if ( isclose(t, Math.atan2(hp.y - oy, hp.x - ox)) )
			selected = HOUR;
		else if ( isclose(t, Math.atan2(mp.y - oy, mp.x - ox)) )
			selected = MINUTE;
		else if ( isclose(t, Math.atan2(sp.y - oy, sp.x - ox)) )
			selected = SECOND;
		else
			selected = NONE;
	}

	/**
	  * Optional mouse drag event for changing the selected time.
	  * WARNING: this method won't do anything without mouseClicked()
	  * or some other way of changing the "selected" field.
	  * @param e the mouse event to handle
	  */
	public void mouseDragged(MouseEvent e) {
		if (selected == NONE)
			return;

		Point mouse = e.getPoint();
		mouse.translate(-canvasWidth/2, -canvasHeight/2);

		double t = Math.atan2(mouse.y, mouse.x) + Math.PI/2;
		if (t < 0)
			t += TWO_PI;

		switch (selected) {
			case HOUR:
				double totalhour = t / TWO_PI * 12;
				hour = (int) totalhour;
				
				double totalminute = (totalhour - hour) * 60;
				minute = (int) totalminute;

				if (hour == 0)
					hour = 12;
				break;
			case MINUTE:
				int temp = minute;
				minute = (int) (t / TWO_PI * 60);

				if (temp == 0 && minute == 59) {
					if (hour > 1)
						hour--;
					else
						hour = 12;
				}
				else if (temp == 59 && minute == 0) {
					if (hour < 12)
						hour++;
					else
						hour = 1;
				}

				break;
			case SECOND:
				int tempsec = second;
				second = (int) (t / TWO_PI * 60);

				if (tempsec == 0 && second == 59) {
					minute--;

					if (minute == -1) {
						minute = 59;

						if (hour > 1)
							hour--;
						else
							hour = 12;
					}
				}
				else if (tempsec == 59 && second == 0) {
					minute++;

					if (minute == 60) {
						minute = 0;

						if (hour < 12)
							hour++;
						else
							hour = 1;
					}
				}

				break;
		}

		// Keeps hours and minutes in their respective ranges.
		// This should not be neccessary. It is only here to prevent chaos.
		hour = Math.max(1, Math.min(hour, 12));
		minute = Math.max(0, Math.min(minute, 59));

		repaint();
	}

	/**
	  * A helper method for determining if two angles are basically the same.
	  * @param a the first angle
	  * @param b the second angle
	  * @return if angle "a" and angle "b" are within 0.1 radians of each other
	  */
	public static boolean isclose(double a, double b) {
		return Math.abs(a - b) < 0.1;
	}

	/**
	  * Paints the clock according to the hour, minute, and second fields
	  * @param g the Graphics object for this panel
	  */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		// Graphics2D has more features than Graphics
		Graphics2D g2 = (Graphics2D) g;

		// Enable antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw background
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, canvasWidth, canvasHeight);

		// draw tickmarks for each hour
		g2.setColor(Color.BLACK);
		g2.setStroke( new BasicStroke(2) );
		for (float t = 0; t < TWO_PI; t += TWO_PI / 12) {
			int x1 = (int) (canvasWidth/2 + rad * Math.cos(t));
			int y1 = (int) (canvasHeight/2 + rad * Math.sin(t));

			int x2 = (int) (canvasWidth/2 + (rad - 10) * Math.cos(t));
			int y2 = (int) (canvasHeight/2 + (rad - 10) * Math.sin(t));

			g2.drawLine(x1, y1, x2, y2);
		}

		// draw hour hand
		Point hourPoint = getHourPoint();
		g2.setStroke( new BasicStroke(10) );
		g2.drawLine(canvasWidth/2, canvasHeight/2, hourPoint.x, hourPoint.y);

		// draw minute hand
		Point minutePoint = getMinutePoint();
		g2.setStroke( new BasicStroke(5) );
		g2.drawLine(canvasWidth/2, canvasHeight/2, minutePoint.x, minutePoint.y);

		// draw second hand
		Point secondPoint = getSecondPoint();
		g2.setColor(Color.RED);
		g2.setStroke( new BasicStroke(3) );
		g2.drawLine(canvasWidth/2, canvasHeight/2, secondPoint.x, secondPoint.y);

		// draw border (circle)
		g2.setColor(Color.BLACK);
		g2.setStroke( new BasicStroke(5) );
		g2.drawOval(canvasWidth/2 - rad, canvasHeight/2 - rad, rad * 2, rad * 2);
	}

	/**
	  * Returns the position of the hour hand in 2-dimensional space
	  * @return the position of the hour hand in 2-dimensional space
	  */
	public Point getHourPoint() {
		double totalHour = hour + (minute / 60.0) + second / (60 * 60);
		double t = totalHour / 12 * TWO_PI - Math.PI/2;

		int x = (int) (canvasWidth/2 + (rad/2) * Math.cos(t));
		int y = (int) (canvasHeight/2 + (rad/2) * Math.sin(t));

		return new Point(x, y);
	}

	/**
	  * Returns the position of the minute hand in 2-dimensional space
	  * @return the position of the minute hand in 2-dimensional space
	  */
	public Point getMinutePoint() {
		double totalMinute = minute + (second / 60.0);
		double t = totalMinute / 60 * TWO_PI - Math.PI/2;

		int x = (int) (canvasWidth/2 + (rad - 20) * Math.cos(t));
		int y = (int) (canvasHeight/2 + (rad - 20) * Math.sin(t));

		return new Point(x, y);
	}

	/**
	  * Returns the position of the second hand in 2-dimensional space
	  * @return the position of the second hand in 2-dimensional space
	  */
	public Point getSecondPoint() {
		double t = second / 60.0 * TWO_PI - Math.PI/2;

		int x = (int) (canvasWidth/2 + (rad - 15) * Math.cos(t));
		int y = (int) (canvasHeight/2 + (rad - 15) * Math.sin(t));

		return new Point(x, y);
	}
}
