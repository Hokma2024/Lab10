
import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main extends JFrame
{
    private JFrame frame;
    private JLabel timeLabel;
    private Clock clock;
    private final DefaultListModel<String> alarmListModel;
    private final List<Alarm> alarms;
    private JList<String> alarmList;

    public Main()
    {

        frame = new JFrame("Часы с будильником");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(563, 376);
        frame.getContentPane().setLayout(new BorderLayout());

        timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setOpaque(true);
        timeLabel.setBackground(Color.BLACK);
        timeLabel.setForeground(Color.GREEN);
        frame.getContentPane().add(timeLabel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        JTextField alarmField = new JTextField(5);
        JButton setAlarmButton = new JButton("Установить будильник");
        JButton deleteAlarmButton = new JButton("Удалить будильник");

        controlPanel.add(new JLabel("Будильник (чч:мм):"));
        controlPanel.add(alarmField);
        controlPanel.add(setAlarmButton);
        controlPanel.add(deleteAlarmButton);
        frame.getContentPane().add(controlPanel, BorderLayout.CENTER);

        alarmListModel = new DefaultListModel<>();
        alarmList = new JList<>(alarmListModel);
        alarmList.setFont(new Font("Arial", Font.PLAIN, 14));
        alarmList.setBorder(BorderFactory.createTitledBorder("Будильники"));
        frame.getContentPane().add(new JScrollPane(alarmList), BorderLayout.SOUTH);

        alarms = new ArrayList<>();

        clock = new Clock();
        clock.addObserver(time ->
        {
            timeLabel.setText(time.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString());

            updateAlarmList();
        });

        setAlarmButton.addActionListener(e ->
        {
            try
            {
                String[] parts = alarmField.getText().split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour < 0 || hour > 23 || minute < 0 || minute > 59)
                {
                    throw new IllegalArgumentException();
                }

                LocalTime alarmTime = LocalTime.of(hour, minute);

                final Alarm[] alarmHolder = new Alarm[1];
                alarmHolder[0] = new Alarm(alarmTime, () -> this.onAlarmTriggered(alarmHolder[0]));
                alarms.add(alarmHolder[0]);
                clock.addObserver(alarmHolder[0]);

                alarmField.setText("");
                JOptionPane.showMessageDialog(frame, "Будильник установлен на " + alarmTime);
                updateAlarmList();
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(frame, "Неверный формат времени! Используйте чч:мм.");
            }
        });

        deleteAlarmButton.addActionListener(e ->
        {
            int selectedIndex = alarmList.getSelectedIndex();
            if (selectedIndex != -1) {
                alarms.remove(selectedIndex);
                updateAlarmList();
                JOptionPane.showMessageDialog(frame, "Будильник удален.");
            }
            else
            {
                JOptionPane.showMessageDialog(frame, "Выберите будильник для удаления.");
            }
        });

        clock.start();
        frame.setVisible(true);
    }

    private void updateAlarmList()
    {
        alarmListModel.clear();
        LocalTime now = LocalTime.now();

        Iterator<Alarm> iterator = alarms.iterator();
        while (iterator.hasNext())
        {
            Alarm alarm = iterator.next();
            Duration duration = Duration.between(now, alarm.getAlarmTime());

            if (duration.isNegative())
            {
                SwingUtilities.invokeLater(() ->
                {
                    JOptionPane.showMessageDialog(frame, "Будильник сработал! Время: " + alarm.getAlarmTime());
                });
                iterator.remove();
                continue;
            }

            alarmListModel.addElement("Будильник: " + alarm.getAlarmTime() + " (через " + formatDuration(duration) + ")");
        }
    }

    public void onAlarmTriggered(Alarm alarm)
    {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, "Будильник сработал! Время: " + alarm.getAlarmTime());
        });
        updateAlarmList();
    }

    private String formatDuration(Duration duration)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Main::new);
    }
}
