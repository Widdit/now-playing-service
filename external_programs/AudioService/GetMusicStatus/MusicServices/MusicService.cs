using CSCore.CoreAudioAPI;

public abstract class MusicService
{
    public virtual void Init() {}

    public abstract string GetMusicStatus(AudioSessionManager2 sessionManager);
}