using System;
using System.IO;

internal static class YesPlayCover
{
    private const string Output = "cover_base64.txt";
    private const string DefaultCover = "no_cover_base64.txt";
    private const string Staging = "cover_base64.yesplay.tmp";

    internal static void Write(byte[] image = null, string mimeType = null)
    {
        if (image == null)
            File.Copy(DefaultCover, Staging, overwrite: true);
        else
        {
            // SMTC can supply JPEG or PNG bytes without a usable content type.
            mimeType ??= image.Length >= 3 && image[0] == 0xff &&
                image[1] == 0xd8 && image[2] == 0xff ? "image/jpeg" : "image/png";
            File.WriteAllText(Staging, $"data:{mimeType};base64," + Convert.ToBase64String(image));
        }
        File.Move(Staging, Output, overwrite: true);
    }

    internal static void Reset()
    {
        File.Delete("cover_base64.lock");
        Write();
    }
}
