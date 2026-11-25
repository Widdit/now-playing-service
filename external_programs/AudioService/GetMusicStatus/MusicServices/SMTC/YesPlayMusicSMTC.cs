using System;
using System.Linq;
using CSCore.CoreAudioAPI;
using Windows.Media.Control;
using Windows.Storage.Streams;

internal sealed class YesPlayMusicSMTC : MusicService
{
    private const string SessionId = "YesPlayMusic.exe";
    private static readonly TimeSpan ArtworkWait = TimeSpan.FromMilliseconds(1500);
    private GlobalSystemMediaTransportControlsSessionManager manager;
    private (string Title, string Artist, string Album)? track;
    private DateTimeOffset artworkDeadline;
    private bool hasArtwork;
    private string output = "None";

    public override void Init()
    {
        YesPlayCover.Reset();
        manager = GlobalSystemMediaTransportControlsSessionManager.RequestAsync().GetAwaiter().GetResult();
    }

    public override string GetMusicStatus(AudioSessionManager2 sessionManager)
    {
        var session = manager.GetSessions().FirstOrDefault(candidate =>
            string.Equals(candidate.SourceAppUserModelId, SessionId, StringComparison.OrdinalIgnoreCase));
        var playback = session?.GetPlaybackInfo();
        var status = playback?.PlaybackStatus;
        if (status is not (GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing
            or GlobalSystemMediaTransportControlsSessionPlaybackStatus.Paused))
            return ClearTrack();

        var media = session.TryGetMediaPropertiesAsync().GetAwaiter().GetResult();
        if (string.IsNullOrWhiteSpace(media?.Title))
            return ClearTrack();

        if (!UpdateArtwork(media))
            return output;

        var timeline = session.GetTimelineProperties();
        int duration = (int)timeline.EndTime.TotalSeconds;
        double seconds = timeline.Position.TotalSeconds;
        if (status == GlobalSystemMediaTransportControlsSessionPlaybackStatus.Playing &&
            timeline.LastUpdatedTime != default)
            seconds += Math.Max(0, (DateTimeOffset.UtcNow - timeline.LastUpdatedTime).TotalSeconds) * (playback.PlaybackRate ?? 1);
        int position = duration > 0 ? (int)Math.Clamp(seconds, 0, duration) : -1;
        output = $"{status}\r\n{Clean(media.Title)} - {Clean(media.Artist)}\r\nProgress:{position}|{(position < 0 ? -1 : duration)}";
        return output;
    }

    private bool UpdateArtwork(GlobalSystemMediaTransportControlsSessionMediaProperties media)
    {
        var current = (media.Title, media.Artist, media.AlbumTitle);
        if (track != current)
        {
            track = current;
            hasArtwork = false;
            artworkDeadline = DateTimeOffset.UtcNow + ArtworkWait;
            YesPlayCover.Write();
        }
        if (!hasArtwork && media.Thumbnail != null)
        {
            using var stream = media.Thumbnail.OpenReadAsync().GetAwaiter().GetResult();
            using var reader = new DataReader(stream);
            uint size = checked((uint)stream.Size);
            uint loaded = reader.LoadAsync(size).GetAwaiter().GetResult();
            var bytes = new byte[loaded];
            reader.ReadBytes(bytes);
            YesPlayCover.Write(bytes);
            hasArtwork = true;
        }
        // YesPlayMusic publishes metadata first and the thumbnail in a later update.
        return hasArtwork || DateTimeOffset.UtcNow >= artworkDeadline;
    }

    private string ClearTrack()
    {
        if (track.HasValue)
            YesPlayCover.Write();
        track = null;
        return output = "None";
    }

    private static string Clean(string value) => (value ?? "").Replace('\r', ' ').Replace('\n', ' ').Trim();
}
