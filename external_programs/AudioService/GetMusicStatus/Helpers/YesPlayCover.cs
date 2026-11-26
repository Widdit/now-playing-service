using System;
using System.IO;

internal static class YesPlayCover
{
    private const string Output = "cover_base64.txt";
    private const string DefaultCover = "no_cover_base64.txt";
    private const string Staging = "cover_base64.yesplay.tmp";

    internal static void Write(byte[] png = null)
    {
        if (png == null)
            File.Copy(DefaultCover, Staging, overwrite: true);
        else
            File.WriteAllText(Staging, "data:image/png;base64," + Convert.ToBase64String(png));
        File.Move(Staging, Output, overwrite: true);
    }

    internal static void Reset()
    {
        File.Delete("cover_base64.lock");
        Write();
    }
}
