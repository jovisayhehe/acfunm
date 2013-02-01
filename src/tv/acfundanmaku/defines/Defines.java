package tv.acfundanmaku.defines;

public class Defines
{
	public final static int PLAYER_PLAYING = 1;
	public final static int PLAYER_PAUSED = 2;
	public final static int PLAYER_STOPPED = 3;
	public final static int PLAYER_ERROR = 4;
	public final static int PLAYER_COMPLETED = 5;
	public final static int PLAYER_BUFFERING_START = 6;
	public final static int PLAYER_BUFFERING_END = 7;
	public final static int PLAYER_INIT_SUCCESS = 8;
	public final static int PLAYER_INIT_FAILED = 9;
	public final static int PLAYER_FINISH = 10;
	public final static int PLAYER_CHANGE_VIDEO_RATE = 11;
    public final static int PLAYER_UPDATE_TIMER = 12;
	public final static int PLAYER_HIDE_TOOLBAR = 13;
	public final static int PLAYER_SEEKED = 14;
	
	public final static int MAIN = 1;
	public final static int FAVOURITE = 2;
	public final static int LIVE = 3;
	public final static int SEARCH = 4;
	public final static int SETTING = 5;

	public final static int ANIME = 1;	
	public final static int GAME_MISC = 2;
	public final static int GAME_MUGEN = 3;
	public final static int MUSIC = 4;
	public final static int DRAMA = 5;
	public final static int SPORTS = 6;
	public final static int FILM = 7;
	public final static int FUN_MISC = 8;
	public final static int FUN_TECH = 9;
    public final static int ARTICLE_MISC = 10;
    public final static int ARTICLE_WE = 11;
    public final static int ARTICLE_DN = 12;
    public final static int ARTICLE_CS = 13;
    
	public final static int ASYNC_SUCCESS = 1;
	public final static int ASYNC_FAILED = 2;
	public final static int ASYNC_START = 3;
    
    public final static int FAVOURITE_ONLINE = 1;
    public final static int FAVOURITE_OFFLINE = 2;
    public final static int FAVOURITE_DOWNLOAD = 3;

	public final static int ACTION_NONE = 0;
	public final static int ACTION_UPDATE_DANMAKU = 1;
	public final static int ACTION_PARSE_URL = 2;
	public final static int ACTION_PARSE_VID = 3;
	public final static int ACTION_APPEND_DOWNLOAD = 4;
	public final static int ACTION_REMOVE_DOWNLOAD = 5;
	public final static int ACTION_CONTINUE_DOWNLOAD = 6;
	public final static int ACTION_DOWNLOADING = 7;
	
	public final static int DOWNLOAD_EMPTY = 0;
	public final static int DOWNLOAD_FAILED = 3;
	public final static int DOWNLOAD_PROGRESS = 1;
	public final static int DOWNLOAD_SUCCESS = 2;
	public final static int DOWNLOAD_UPDATEINFO = 10;
	
	public final static int FROM_NONE = 0;
	public final static int FROM_HISTORY = 1;
	public final static int FROM_FAVOURITE = 2;
	public final static int FROM_DOWNLOAD = 3;
	
	public final static int PLAY_HW = 1;
	public final static int PLAY_SW = 2;

	public final static int LISTTYPE_FAVOURITE = 1;
	public final static int LISTTYPE_DOWNLOAD = 2;
	public final static int LISTTYPE_HISTORY = 3;
	
	public final static int MSG_LOGIN = 1;
    public final static int MSG_LOGOUT = 2;
    public final static int MSG_LOGIN_SUCCESS = 3;
    public final static int MSG_LOGIN_FAILED = 4;
    public final static int MSG_LOGOUT_SUCCESS = 5;
    public final static int MSG_POST_SUCCESS = 7;
    public final static int MSG_POST_FAILED = 8;
    public final static int MSG_WRONG_INPUT = 9;
    public final static int MSG_READY = 15;
	public final static int MSG_ERROR = 16;
	public final static int MSG_SHOW_PROGRESS = 14;
	public final static int MSG_HIDE_PROGRESS = 15;

	public final static int PARSE_MP4 = 1;
	public final static int PARSE_FLV = 2;
	
	public final static int RATIO_FIT = 0;
	public final static int RATIO_FILL = 1;
	public final static int RATIO_ORIGINAL = 2;
	
	public final static int DANMAKU_SCROLLING = 1;
	public final static int DANMAKU_BOTTOM = 4;
	public final static int DANMAKU_TOP = 5;
}
