package tv.avfun.entity;

public class Favorite {

	public String title;
	public String aid;
	public String username;
	public String userid;
	public int type;
	public int channelid;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getTpye() {
		return type;
	}
	public void setTpye(int tpye) {
		this.type = tpye;
	}
    
    public int getChannelid() {
        return channelid;
    }
    
    public void setChannelid(int channelid) {
        this.channelid = channelid;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aid == null) ? 0 : aid.hashCode());
        result = prime * result + channelid;
        result = prime * result + ((userid == null) ? 0 : userid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Favorite other = (Favorite) obj;
        if (aid == null) {
            if (other.aid != null)
                return false;
        } else if (!aid.equals(other.aid))
            return false;
        if (channelid != other.channelid)
            return false;
        if (userid == null) {
            if (other.userid != null)
                return false;
        } else if (!userid.equals(other.userid))
            return false;
        return true;
    }
	
	
	
}
