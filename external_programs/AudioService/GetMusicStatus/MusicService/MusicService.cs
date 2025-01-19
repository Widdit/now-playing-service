using System;
using CSCore.CoreAudioAPI;

public abstract class MusicService
{
    public virtual void Init() {}

    public abstract void PrintMusicStatus(AudioSessionManager2 sessionManager);
}