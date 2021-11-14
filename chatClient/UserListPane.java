package chatClient;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.*;

public class UserListPane extends JPanel implements UserStatusListener {
	
	private final ChatClient client;
	private JList<String> userListUI; // to add stuff and remove it needs a listmodel
	private DefaultListModel<String> userListModel;

	public UserListPane(ChatClient client) {
		this.client = client;
		this.client.addUserStatusListener(this); //add itself as a listener
		
		userListModel = new DefaultListModel<>();
		userListUI = new JList<>(userListModel);
		setLayout(new BorderLayout());
		add(new JScrollPane(userListUI), BorderLayout.CENTER);
		
		userListUI.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() > 1)
				{
					String login = userListUI.getSelectedValue();
					MessagePane messagePane = new MessagePane(client, login);
					
					JFrame f = new JFrame("Message: " + login);
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.setSize(500, 500);
					f.getContentPane().add(messagePane, BorderLayout.CENTER);
					f.setVisible(true);
					
				}
			}
		});
	}

	public static void main(String[] args)
	{
		ChatClient client = new ChatClient("192.168.0.239", 5331);
		
		UserListPane userListPane = new UserListPane(client);
		JFrame frame = new JFrame("Tae" + "'s User List");        //change this
		frame.setDefaultCloseOperation((JFrame.EXIT_ON_CLOSE));
		frame.setSize(400, 600);
		
		frame.getContentPane().add(userListPane, BorderLayout.CENTER); //frame.add is shortcut for frame.getContentPane.add
		frame.setVisible(true);
		
		
		if(client.connect())
		{
			try {
				client.login("tae", "xyz");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void online(String login) {
		userListModel.addElement(login);
		
	}

	@Override
	public void offline(String login) {
		userListModel.removeElement(login);
		
	}
}
