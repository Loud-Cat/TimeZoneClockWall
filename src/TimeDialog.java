import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;

/**
  * This dialog contains a virtual analog clock that
  * allows the user to select a time by clicking and dragging
  * the hour, minute, and second hands, as well as toggling a PM checkbox.
  */
public class TimeDialog extends JDialog {
	private JPanel panel;
	public final GraphicsPanel gp;

	public final JLabel label_title, label_selected;
	public final JButton btn_submit, btn_cancel;
	public final JCheckBox checkbox_pm;

	/**
	  * Initializes the dialog and all of its components.
	  */
	public TimeDialog(JFrame owner, String title) {
		super(owner, title);

		// The digial analog clock
		gp = new GraphicsPanel();

		// A listener for when the user selects a hand and drags it
		MouseAdapter listener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				gp.mousePressed(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				gp.mouseDragged(e);
				updateTime();
			}
		};

		// Mouse drag events need both of these listeners to work
		gp.addMouseListener(listener);
		gp.addMouseMotionListener(listener);

		Font font = new Font("Helvetica", Font.PLAIN, 14);

		label_title = new JLabel("Use the clock to select a time.");
		label_title.setFont(font);

		label_selected = new JLabel("Time:");
		label_selected.setFont(font);

		btn_submit = new JButton("Submit");
		btn_submit.setFont(font);

		btn_cancel = new JButton("Cancel");
		btn_cancel.setFont(font);

		// When the user clicks "cancel", close the dialog
		btn_cancel.addActionListener(evt -> {
			setVisible(false);
		});

		checkbox_pm = new JCheckBox("PM");
		checkbox_pm.setFont(font);

		// Update the preview text when the user toggles the "PM" checkbox
		checkbox_pm.addActionListener(evt -> {
			updateTime();
		});

		// ---------- LAYOUT ----------

		// GridBagLayout is an alternative grid based layout manager
		// that allows more advanced control of "cell" component layout
		// at the cost taking a little bit more to setup
		panel = new JPanel( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(5, 5, 5, 5);
		c.weightx = 1.0;
		c.gridwidth = 3;

		panel.add(label_title, c);

		c.fill = GridBagConstraints.NONE;

		c.gridy = 1;
		panel.add(gp, c);

		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 2;
		panel.add(label_selected, c);

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.gridy = 3;

		c.anchor = GridBagConstraints.LINE_START;
		panel.add(checkbox_pm, c);

		c.gridy = 4;
		panel.add(btn_submit, c);

		c.gridx = 2;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(btn_cancel, c);

		updateTime();

		add(panel);
		pack();
	}

	/**
	  * Updates the preview text to display the selected time
	  */
	public void updateTime() {
		String time = String.format(
			"Time: %d:%02d:%02d %s",
			gp.hour, gp.minute, gp.second,
			checkbox_pm.isSelected() ? "PM" : "AM"
		);

		label_selected.setText(time);
	}

	/**
	  * Returns the 12-hour time entered by the user in 0-23 hour format
	  * @return the selected hour, from 0-23
	  */
	public int getHour() {
		int out = gp.hour;

		if ( checkbox_pm.isSelected() )
			out = (out + 12 < 24) ? (out + 12) : 12;
		else if (out == 12)
			out = 0;

		return out;
	}

	/**
	  * Returns the selected minute
	  * @return the selected minute
	  */
	public int getMinute() {
		return gp.minute;
	}

	/**
	  * Returns the selected second
	  * @return the selected seond
	  */
	public int getSecond() {
		return gp.second;
	}
}
