package chatClient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class MessagePane extends JPanel implements MessageListener {

	private final String login;
	private final ChatClient client;

	private DefaultListModel<String> listModel = new DefaultListModel<>();
	private JList<String> messageList = new JList<>(listModel);
	private JTextField inputField = new JTextField();

	public MessagePane(ChatClient client, String login) {
		this.client = client;
		this.login = login;

		client.addMessageListener(this);

		setLayout(new BorderLayout());
		add(new JScrollPane(messageList), BorderLayout.CENTER);
		add(inputField, BorderLayout.SOUTH);

		inputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) // pressing enter
			{
				try {
					String text = inputField.getText();
					client.msg(login, text);
					listModel.addElement("You: " + text);
					inputField.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onMessage(String fromLogin, String msgBody) {
		if (login.equalsIgnoreCase(fromLogin.substring(0, fromLogin.length()-1))) {//parse out the :
			String line = fromLogin + " " + msgBody;
			listModel.addElement(line);
		}
	}

}
