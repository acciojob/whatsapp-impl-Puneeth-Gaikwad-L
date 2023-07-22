package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public int getCustomGroupCount() {
        return customGroupCount;
    }

    public void setCustomGroupCount(int customGroupCount) {
        this.customGroupCount = customGroupCount;
    }

    public String createUser(String name, String mobile)throws Exception{
        if(userMobile.contains(mobile)){
            throw new RuntimeException("User already exists");
        }else {
            userMobile.add(mobile);
            User user = new User(name,mobile);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users) {
        customGroupCount++;
        String groupName = "Group "+customGroupCount;
        Group group = new Group(groupName,users.size());
        adminMap.put(group,users.get(0));
        groupUserMap.put(group, users);
        return group;
    }

    public int createMessage(String content) {
        messageId++;
        Message message = new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(!adminMap.containsKey(group)){
            throw  new RuntimeException("Group does not exist");
        }else {
            List<User> list= groupUserMap.get(group);
            for(User u : list){
                if(u == sender){
                    senderMap.put(message,sender);

                    List<Message> groupList = groupMessageMap.getOrDefault(group, new ArrayList<Message>());
                    groupList.add(message);
                    groupMessageMap.put(group, groupList);
                    return groupList.size();
                }
            }
            throw new RuntimeException("You are not allowed to send message");
        }

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!adminMap.containsKey(group)) {
            throw new RuntimeException("Group does not exist");
        }
       User admin = adminMap.get(group);
        if(admin != approver){
            throw new RuntimeException("Approver does not have rights");
        }
        List<User> groupList = groupUserMap.get(group);
        for(User s : groupList){
            if(s == user){
                adminMap.remove(group,approver);
                adminMap.put(group, user);
                return "SUCCESS";
            }
        }
        throw new RuntimeException("User is not a participant");
    }

    public int removeUser(User user) {
        Boolean userfound = false;
        Group userGroup =null;
        for (Group group  : groupUserMap.keySet()){
            List<User> participants = groupUserMap.get(group);
            for (User participant: participants){
                if(participant.equals(user)){
                    if (adminMap.get(group).equals(user)){
                        throw new RuntimeException("Cannot remove admin");
                    }
                    userGroup = group;
                    userfound = true;
                    break;
                }
            }
            if (userfound){
                break;
            }
        }
        if (userfound){
            List<User> users= groupUserMap.get(userGroup);
            List<User> updatedUsers = new ArrayList<>();
            for(User participant: users) {
                if (participant.equals(user)) {
                    continue;
                }
                updatedUsers.add(participant);
            }
                groupUserMap.put(userGroup, updatedUsers);

                List<Message> messages = groupMessageMap.get(userGroup);
                List<Message> updatedMessage = new ArrayList<>();
                for (Message message : messages){
                    if (senderMap.get(message).equals(user))
                        continue;
                    updatedMessage.add(message);
                }
                groupMessageMap.put(userGroup,updatedMessage);

                HashMap<Message, User> updatedSenderMap = new HashMap<>();
                for (Message message:senderMap.keySet()){
                    if (senderMap.get(message).equals(user))
                        continue;
                    updatedSenderMap.put(message, senderMap.get(message));
                }
                senderMap = updatedSenderMap;
                return  updatedSenderMap.size() + updatedMessage.size() + updatedSenderMap.size();
        }
        throw new RuntimeException("User not found");
    }
}
