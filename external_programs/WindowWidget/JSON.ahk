;
; cJson.ahk 2.1.0
; Copyright (c) 2023 Philip Taylor (known also as GeekDude, G33kDude)
; https://github.com/G33kDude/cJson.ahk
;
; 0BSD License
;
; Permission to use, copy, modify, and/or distribute this software for
; any purpose with or without fee is hereby granted.
;
; THE SOFTWARE IS PROVIDED “AS IS” AND THE AUTHOR DISCLAIMS ALL
; WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES
; OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE
; FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY
; DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
; AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT
; OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
;
#Requires AutoHotkey v2.0

class JSON
{
    static version := "2.1.0-git-built"

    /**
     * When true, Boolean values in the JSON will be decoded as numbers 1 and 0
     * for true and false respectively.
     *
     * When false, Boolean values in the JSON will be decoded as references to
     * {@link JSON.True} and {@link JSON.False} for true and false respectively.
     *
     * By default, this property is true.
     */
    static BoolsAsInts {
        get => this.lib.bBoolsAsInts
        set => this.lib.bBoolsAsInts := value
    }

    /**
     * When true, null values in the JSON will be decoded as ''.
     *
     * When false, null values in the JSON will be decoded as references to
     * {@link JSON.Null}.
     *
     * By default, this property is true.
     */
    static NullsAsStrings {
        get => this.lib.bNullsAsStrings
        set => this.lib.bNullsAsStrings := value
    }

    /**
     * When true, unicode values in the JSON will be encoded using backslash
     * escape sequences, such as '💩' will be encoded as "\ud83d\udca9". This
     * is to improve compatibility with external systems.
     *
     * When false, unicode values will be left as their original characters.
     *
     * By default, this property is true.
     */
    static EscapeUnicode {
        get => this.lib.bEscapeUnicode
        set => this.lib.bEscapeUnicode := value
    }

    /**
     * Utility function for the MCode to convert non-string values to string.
     */
    static fnCastString := Format.Bind('{}')

    /**
     * Constructor
     */
    static __New() {
        this.lib := this._LoadLib()

        ; Populate globals
        this.lib.objTrue := ObjPtr(this.True)
        this.lib.objFalse := ObjPtr(this.False)
        this.lib.objNull := ObjPtr(this.Null)

        this.lib.fnGetMap := ObjPtr(Map)
        this.lib.fnGetArray := ObjPtr(Array)

        this.lib.fnCastString := ObjPtr(this.fnCastString)
    }

    /**
     * Internal function to load the MCode
     */
	
	static _LoadLib32Bit() {
		static lib, code := Buffer(9904), codeB64 := ""
		. "G7gAVVdWU4HsbAFAAACLvCSMAWC0RCSAATCcJIQAMIkA+IhEJDMPtwYAZoP4Aw+EBgiEAAAAJBQPhEQDJBAID4SiAxIFD4QCwAMSCQ+EhgAAQACF"
		. "2w+ETgAiiwgDvV8AHIPGCI0AUB7HACIAVQBAx0AEbgBrAAwICG4AbwAGDHcAbiEABhBfAFYABhRhBABsAAYYdQBlAACJE2aJaByLhAQkiAGMXCQE"
		. "iUQAJAiJNCTolCABAVmNUAKJE7oiAQBeZokQMfaBxAEByInwW15fXcMEjbYAFgCLRgg7AAXQHgAAD4QpQAkAADsFyAIL5SoKAQvMAgs5gAuLEACN"
		. "TCRQiQQkiQhMJBQABVTHRCSgVO4fAACAAxCBI1WAAwyALQCADwiABQQC1AAd/1IUg+wYAItEJFCD+P8PwIRhBgAAugB4gRUCYIEZjWwkcGaJEFQk"
		. "YI2AAYmUJCoggatWAR9kAk0KiSBsJBiNrIILiRRKJIALbAQ1aAIAb8cYhCQkATkBCYQkLF0HBSgABYEGABRwBBh0dYQDeIQDfIQDACOCAxzbgQMA"
		. "NxSBZ4QdDIQNgSyCAMBVBP9RGLkBA4CD7CSNhCSAAxsLgQECCITDL0YIZolCjEIFjYwkkMMIhMnECIsQgEcYi0BSAAWijAUOTCQEgAOIgAKuDEM1"
		. "QQ2EMpSHApiHAgachQIpMAQk/1IYtrjELwARoAQRgCmwAohfgwRANoEBhzOACqSHHqx1kTKoAAQUwy6BEoQKtPWHAriHArzxLoBIwy+ALqBmg3wk"
		. "cMDackLTdrxCS4AD80CpgQMBIAMwD4X0A8AoAx6FwHPAs4EDRgjAOgEYgCHAVUdfOIADxIk1FIAJMPWDCcgHLsxHAYED4h2PX248CWBiFmJghOIO"
		. "wmBaXUERbkERYRCgCUAnV0SlRwFIRAGLVcEKGMAFG+EEAANMZQQNKEwkNFOBFzlYiSyGKLzCDwk4D4UKACdgBKIQuntJAwZEAgSU2QvCiEgIAokL"
		. "IIiJ+oTSAHQcg8AEiQO4eg1iRgFljOCFYYwAHZBBoXV8JEgx9kB34BWDGdBnJ9REAYPAAdWgAdjFMMeAAdynBOEGVUQB5EcB6EcB7EcB8HVHAfRH"
		. "AfxHAYKBQwEEX0cBIjZDAeIlQwEURwEYRUcBHEQBuQxAYBWsvCQI4AHBfCELYGA4wB7vQQEgA8E2AAY0BAbAaAEr9+ADIQ8AAzwHA4E9AAPhEL0A"
		. "AzjAAKEuAAMhPLghDd9hcqEbYHIhKKAB+CFCgwLdoAFQIwihHqABVKABJwWuWGMDwSCgAVyhAUQARO+DVwADIVuGeDyQSRZJoVvnJ4qASQBshZrE"
		. "byEGwG8UhAegC5RCKIXSDwSEdcABhfYPhFYLYF/BSkvh3hOAfCSQMwCNSmBLuSzCRzAKD4XpIAMAfEAByA+GcYAFD7bA66BJUBCDxgHE2wyl3SwC"
		. "JIHd6Fr4///pAGP+//+NdCYAipChC7gAzYsDuWG+or9B641QIP3qT+HqiGIAauHqZQBjYBHkSBxh63geUEOCB4Mc+MH4H5cbUgggCEAXsQE58BDp"
		. "hMAIcghkAzH2A48DjwMEJOjkGADyAOt5tCbiP9MC0IFSCGw0JBAFUQ+0/wL8AoPMAA/mAiUb6H1gAyUC4AAB6ev34ApzgCuEB+ALkANwCcwaAADp"
		. "JsRhAvMFuAVAAPIP/hByYaEkJGTBYuEqIQFgDfNhAJQPobyQUCgxaE7zJz+vTeRPxCePKY8pjSnyDx4RIzUTKqAoQYEPtwJCZtFg8vb//4Eihxkx"
		. "CBO5YTfkDZCJ1sCDwgJmiQbwEBMDEAQIg8FRA3XliRgT6bmAA/MShdt0oHeLA751MSZlMiZACMcAdAByQBBwIgSSIwbpi2IGdE9HEJ6BiyICbgB1"
		. "IAJoBSACujEBZolQBukGY3AC9BeLCI1CAgEBJIPAAoPBAWZAg3j+AHXzxRkIBOk4pwKDAATpKUP2CLI0D4TB/PABlLMyWHA3iLLgAIEOhBExMhNx"
		. "BYnRgFchDscBQAkAOfh171ANjLNQAvUPD7cTRPOsw/AUbfOsc3Afs6stsDyTrVVLUAXRBVjBPgO5YxAci682qzZUoTZ5AHAxNo4YYBAROmAQGg+/"
		. "tEuIRCRY0TJEJFxMKWaNkAExKbgVvq1hCIlzwApiAbk6BRGBGIAKtjr7AA5QMRexJMAGAuluoyEB8BCBA/wRefULCiDHAGYAYfELbACCcwIESAjp"
		. "wfR2MvswpCEkgiAxaIQohF+DtCl8QP8AAMF1dLfHiJEuJv+gCcSI5WjGiLJlc4X6hppY7BS5YAywnRjkN/g5wBP+PJVo86liOhaIpTn6OPWE/otm"
		. "CrVr2Go7i/87/jshDE9xCi+eKp7UiXVw8RlUg/EZcBOD+AEZ0vDCAECD4uCDwnvpnmMwGCAigbEhGf71LxnPLxkrGXQAMgzpaEBk8Qb6CvIgv/Fj"
		. "otn/Y/9j/mM6ePJjaP9jb2CBeem+6vkgJnbHWRCht/UdzwMhP2Qk6KASyl0M8tD//4nGBT2LIJkDPObBgBsBPHUTwAIBmHEXxBXUwNyD7AThAsGX"
		. "faMCrakC0ZepAkeEcRsI1In4MAGEcBiWwUfRVY/AA6BPgaKyOmaJKFM7oMGJE4nQFKMR4CUB0krJfh8x0maQGInBv5CE4k/CAWYIiTk5w0x154kD"
		. "WWCBRAGgqDAgCxIgfTGAP+ko8RJPQg8CAR1kULqQIdAAIADp7vrNQlgqDEOkD+mOcEA2E1AF6djw6AABkADmuQDYAAAAiUQkWAjB+B8AYFyLhCQA"
		. "iAEAAMdEJAQTAMAByAiNAHSJBCQg6CIRAAAEeIMAAgEEJIsAjVACgwDAAYB8JDMADxBFwouUAiyJAukAIvf//41KBIkIC7kNAJBmiUoCoIsTjUoC"
		. "AB4KAh4ACuk0+v//hduID4SCAUcDuSIAFkEATIkTZokIBF2JyFwkBAKChCQCpwCFNJwQACwTAiwAQ41CCALp6wBGicbpAArwA00lAk3pr/7/Av8H"
		. "rYn4hMAPhEw/9AkTAALpLQkREIiLRggGfIQkQAAKcYGKhCREASmESIBGi2hEJDQBRREBRQYn6Qx974YlgIoUB422QwEkgI050HX5h4v/HvUGEwVv"
		. "AAcFbz0SAJgA6Q+AaIQJixCACgD/UgiD7ATpQWr9AgvoDAtVAwuCO5wEJJAGboXbfhyLQBAxwIPCAQBCO1KEAg118YU9EAhdxF7uCktHXIZDwX8P"
		. "hjuLJcFBTEAV6f4AH4XbFHRQwG90wm8QxwAAIgBPAMdABGKEAGqAAQhlAGNAakBIDIkTul8CbVAiDsQ26fL4iB0OD1C/hCTQgQVtBzSDUAAI6VeA"
		. "A5AGAFUgV1ZTuxSACYHsIowBQoQkpIEBtCQaoAIXGEAcASzHQAwBgQGLBg+3GI1TAPdmg/oXdyi5ABMAgAAPo9FzCjzAmb8BA2aQD7cgGonQjUtA"
		. "CPkXCA+G7kFGBmaD+zB7D4QeQD5AAlsPDITpQAVAAiIPhEISBsA4U9CAEwkPlgLBwAQtD5TCCNEAiEwkNw+EkAWHxiNCKE8jPg+3HwINKIS5BwJX"
		. "OEAWANka6EACPAEIwAYwD4QCMsAMjUPPZoP4oAgPh3cDBhW9Ac4AifmLUAyLQAgIjbQmQQyQg8ECBIu8ggeJDmvKCgD35QHKD7/LiQDLwfsfAcgR"
		. "2gCDwNCD0v+JRwAIiVcMiw4PtwgZjXtAL/8JdseCiQAY+y4PhFMgEQiD49+AAUUPhbQBRyqNVwKJFmaDADgUdRDfaAi6A2AbABoQ3VgIixZYD7cC"
		. "gBOgGCOgAsYIRCQ3ABf4Kw+EEhTAAY1I4Av5CQ8Ih8IC4ArCAjHJAeRg6DCNDImJ1wFAApiNDEgPt0II/o1YQAX7CXbkgIk+hckPhBXAGwgx0rgh"
		. "Io12AI0EBIAAVwHAOcp1cvSATTDbYAAkE2CNN4AA3UAID4RUwDGM3vFhEiMDD7cQQDOQFA+EeoAaMcBgATAFD4VJABHkA9xIwgiABTHA6TdgAuQS"
		. "AMICD6PPD4L0CaBM6QGAhoPAAo1oTCRQQDJg4QPCRIlQBqHAHgI2ZOI4EFCJTCQYoARg4AAURaACaII8RCRs5AAg9eQAHOQAEIEa4ACiQOAAA4NC"
		. "Z2v/UhiD7CQAi1wkWI1UJExZAANINEAI4ABMwg0D0IlUJBQgA0jgACGUD+COwwnECsMIHCT/UAAUiy6D7BgPt1BVAI1CQFr4QFoLh4AE4ClgIV0P"
		. "hGhhWmCF0g+EXwABhCKJhDQkACwE6KL8QHkwwA+FEqOdYkBUJK5w4Ashe+EMfETCcCMS2otjFXikEI0eGJkf4AceBCIWAh+jDYBNCXUOE2BbqJuL"
		. "LogZdyIPgKPHc3WNRQKkX0PgGkA+icWNSuJ3dggyiS7gHCwPhYADoGYABwKDxQKJLhlFIYf4wDsgCHLl6RwB/4DT5LPhP3K46xbE4wGCQZNAFusN"
		. "3aDY6wnd2EQNuCAGgP/rAt3YgcThjqBbXl9dw0dFcElF3sRDRaImRkXhKHjpKC0mI0RJX0UYixaFRXQm0aAcAo1IpJdAQBQgHSh9D4SBb2aAOYS5"
		. "gQEBg/giD4U7QR7bgFrFPbOh78A9I3ABURZQTQCNQVMWIDASDwSDC/ECRQIPtwjF8BVRElQPho5BCDAW4Pk6D4XqE3aRG2IW01EFwAXoWoQFyvcB"
		. "kB61hHNosgfH4DlhRAolBwobIgcMwAoPt0oCUzA5hAF2ODFI+RAd8OXRYMIgSekKgQn2H/IbkNcPglRQAelhpwEJcR6z6yFadA+EBUPgIJAAZg+E"
		. "opMAbhgPhSvgCWBgZoN4gAJ1iRYPhRvyANIE8AAEbPEAC/IAsGAUeAbyAPsAQoPACDiAPbjgHWAeJUeEwR2gGblhGkBoYiyJCKHU2B8gAkaRRs1g"
		. "A2CPAfBOAokOicrp3MOxfQNbjUgCvXEDcAGgTwhmiS8RERpzZgLmMAWDwATrGZAAZolY/onBifq3oAGgDtMBJvFosY5nwQUQegKJPnABXHXUI7ET"
		. "0BIidFpQAFx0QmlQAC8PhKGiNfmwYg+E2ZMAYBAGEm0g+W4PhHwCIvlyCA+EETMBdA+F84kwAbsJwlEEicESCIgW6XuxFXYAv5GXlXQBeHEBY3AB"
		. "u1zMAs5OQAESM2JHhcigRaciRrqhBVAkiRCJwliqqdABvS8XBGgRBAzCA0Q6fVIED4WGGATCtAK5EgQWkBYTBGjQAS6/oRSJCfkny1BFKcMkMf8R"
		. "JFj8kGGJObUQAjFgA7uBNqkLk2ADxN7JYGLpp/lQBcGrBVkFdGEhRwLZ6A8Yt18CEXzxCYkG2ZjgicdRfNEA6TVzjZHCCscCuaFziT5AdKfwCrFq"
		. "UYCNQ7Bv+GByn6EpwKGxAqNvcG/rMGADwgFAClwkMN9wb/VvAIk+3vncQgjdClo0BEuidnbM6TxTBwf9j414sCdYICLplg1AAgQvcgEtKvsGL60C"
		. "MBryAAIvZfEACvAA1QIvsAwv+XAKvWALsQaxIQtmiSjkBqAY3fAtAN3Yi0gMi3QkCDiJxyA/PA+vRwAID6/OAcGJ8CD3ZwgBytCIMcC58IjpsbAC"
		. "Qo2JHxMxLbVjCmFhCINAAnk5c/IAVWIKc/EAY/IACPAACEViC1PwAIPACm8LUU2wCr5kC8KWiTBnCyZDUAPAFHUPhRPxBEoYBDHtYDkyCQ+3eggE"
		. "jV+jih2NX78hgAAFD4ZfYXVfnwnCAIffkCKNX6mN4HoGweME0DiQJFADCAaNb1AD/QkPhqr0gR1vkAP9kAPuwgCTkAPAAIelkQNcO6ADqgipAwim"
		. "A7KpA8apAwpqpgMKqANqCo19UROec419YAP/YAOFTWEDfWADwACHM2IDK6Kp4QKDwgwQF6GAHvMyORA5ocxQE4RMAEyUeDYEUMSQxgEBMYMCOKEy"
		. "0IAC69ViQoCuocgR4QDGAfthBeunAXjr6WWQMGAA4WVQCsmk6RqCACvJAgKGIgGCQqEzyDHb6WOAOIk1qIR8ANLpS/YRFQjJ6angNtno6QL/4ADz"
		. "vwEnNQAPAA8ADwAPAP8PAA8ADwAPAA8ADwAPAA8ADw8ADwAPAA8AMDEyMwA0NTY3ODlBQgBDREVGAABIAABhAHMATQBlAAB0AGgAbwBkAK60AAAA"
		. "UAB1AHMAAGgAAABTAGUAgnQAOE8AdwBuALggcgBvAHAAbAAAil8ABEUATHUAbQA0AQfEAABWU4PsdBCLnCSAABaLtCQCiAAMjVQkPMdEBCQ8ATyL"
		. "A4lUJGAUjZQkhAAYABQITQAwEAEwAA4MAQMOBAEBD4kcJP9QFA8AtwaLVgyD7BgAZolEJFCLRggVADJcAE9AAA5YuAhjACcBGGCLhAJPADtEYQI7"
		. "RCRojQAvAAdAlIsDABVMBGFIAgMH6iAEDxwEBxgCNQCYAY0fAT+BRoQNATKLShiD7IAkZoM+CXUOAEgAixCJBCT/UggAg+wEg8R0W14Ew5ALAFUx"
		. "7VdWEFOB7JwBfoQksBECWawkjgEHCItYQASJyInaBQAGgACD0gCD+gAPhxIjgIK7FIAIvs3MAMzMhcl4fY10ACYAkInIg+sBAPfmweoDjQSSAAHA"
		. "KcGNQTCJAtGAflxmhdJ14TCLjCS0ArUAB8kPFIShAS+8AgmDwgIgiw+NtCYBX412CACJywAIg8ECZgCJAw+3Qv5mhRjAdeyAS4EUiQiBAsQBUzHA"
		. "W15fXQjDjbaBDL1nZma4Zr8wAQ/CA8Ar3kEiAO2JyMH4H8H6CAIpwgAkjQRHKQrIhyPahCOD7gK7Ci3AAbgBAWaJXHQQZo1UdAEohV//VP//gB24"
		. "BCgABChmApBBJ8ABZoN6/pgAdfOAMEEIiQcLJgPBQ8B3QIlMJEiNMEwkML+BSwB7VKFUvB7CakCCmhDABxhFwAdUxYVmiXwADVxUJEwABFiEdWDE"
		. "AVz/BJ+KhgAQgoTEBpOEgX+BhICLVCQ4D7cCQFRQD4Sz/oA3tMJAhSj2dEGEYrmBn4sXTcNz00A6AGGLREENBNYIgWTAYujFYhCKPIRKIIsIjUIC"
		. "w2GDwBtAccBJeMJJgweJCOm+OgAey59An8CmACccgBoAGItMJCCF0g+EhI2AIosavyLBdQBzAokyZok7DwC3GGaF2w+EBAOgHedFZoP7Ig+EhgZg"
		. "AiABXA+ETCMBEAgPhGIjAQwPhEJ4IwEKD4QeIwENh6AIAAwgAQkPhKJgAgiAPbQgMwAPhL0BgQtz4GaD/l4PLIbHgAGBERiCEb5cCSAEv3VjG2aJ"
		. "M40EcwThEnsCD7dYAP6J34neid1mAMHrDGbB7gSDAOcPD7fbZsHtAAiJPCRmD76bBNwfoB/3g+UPi6IyYANmiR6AAp2BAlBmiV4CYAGfYQGLAWAF"
		. "iV4EjV4IiZYaZQIAAgYEH4URYFIQhdJ0TeIzizKNIEYCiQK4ISRmiVAGg8QEAjGQAAREGIsavWEVABRmiSvoiTK+YwRzARWAV+A4DYAJxAAxIGG4"
		. "izGDkMYBiTHoB3V0AQxggwEC68/gK+AJ9EdlH4EACR/rrmbhDdRTADVBA71ihQ474QNrqALrjuIDtO0RZuQRXOlrIBpEOWUMbmwM6cZHYQThEA+E"
		. "aIFybAmL4b9iCR/jBI1zgYA3MCEPhkMAHAA8Hw8khjlCHXRgQCZ+AuSJOqAv6fJAAuNU4QqWEOIKYxhybBjpx0EFkHYAiznAMI13QCWBwieF/f//"
		. "6cJAA0HgJIMBBOmhQgEBBOmZ7Vs="
		if (32 != A_PtrSize * 8)
			throw Error("$Name does not support " (A_PtrSize * 8) " bit AHK, please run using 32 bit AHK")
		; MCL standalone loader https://github.com/G33kDude/MCLib.ahk
		; Copyright (c) 2023 G33kDude, CloakerSmoker (CC-BY-4.0)
		; https://creativecommons.org/licenses/by/4.0/
		if IsSet(lib)
			return lib
		if !DllCall("Crypt32\CryptStringToBinary", "Str", codeB64, "UInt", 0, "UInt", 1, "Ptr", buf := Buffer(5816), "UInt*", buf.Size, "Ptr", 0, "Ptr", 0, "UInt")
			throw Error("Failed to convert MCL b64 to binary")
		if (r := DllCall("ntdll\RtlDecompressBuffer", "UShort", 0x102, "Ptr", code, "UInt", 9904, "Ptr", buf, "UInt", buf.Size, "UInt*", &DecompressedSize := 0, "UInt"))
			throw Error("Error calling RtlDecompressBuffer",, Format("0x{:08x}", r))
		for import, offset in Map(['OleAut32', 'SysFreeString'], 8148) {
			if !(hDll := DllCall("GetModuleHandle", "Str", import[1], "Ptr"))
				throw Error("Could not load dll " import[1] ": " OsError().Message)
			if !(pFunction := DllCall("GetProcAddress", "Ptr", hDll, "AStr", import[2], "Ptr"))
				throw Error("Could not find function " import[2] " from " import[1] ".dll: " OsError().Message)
			NumPut("Ptr", pFunction, code, offset)
		}
		for offset in [229, 241, 253, 284, 312, 407, 621, 809, 1049, 1081, 2371, 3177, 3817, 3860, 5416, 5527, 5971, 6450, 6486, 7203, 7386, 7696, 7737, 7752, 8894, 9304, 9398, 9419, 9431, 9451]
			NumPut("Ptr", NumGet(code, offset, "Ptr") + code.Ptr, code, offset)
		if !DllCall("VirtualProtect", "Ptr", code, "Ptr", code.Size, "UInt", 0x40, "UInt*", &old := 0, "UInt")
			throw Error("Failed to mark MCL memory as executable")
		lib := {
			code: code,
		dumps: (this, pObjIn, ppszString, pcchString, bPretty, iLevel) =>
			DllCall(this.code.Ptr + 0, "Ptr", pObjIn, "Ptr", ppszString, "IntP", pcchString, "Int", bPretty, "Int", iLevel, "CDecl Ptr"),
		loads: (this, ppJson, pResult) =>
			DllCall(this.code.Ptr + 4784, "Ptr", ppJson, "Ptr", pResult, "CDecl Int")
		}
		lib.DefineProp("bBoolsAsInts", {
			get: (this) => NumGet(this.code.Ptr + 7856, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7856)
		})
		lib.DefineProp("bEscapeUnicode", {
			get: (this) => NumGet(this.code.Ptr + 7860, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7860)
		})
		lib.DefineProp("bNullsAsStrings", {
			get: (this) => NumGet(this.code.Ptr + 7864, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7864)
		})
		lib.DefineProp("fnCastString", {
			get: (this) => NumGet(this.code.Ptr + 7868, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7868)
		})
		lib.DefineProp("fnGetArray", {
			get: (this) => NumGet(this.code.Ptr + 7872, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7872)
		})
		lib.DefineProp("fnGetMap", {
			get: (this) => NumGet(this.code.Ptr + 7876, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7876)
		})
		lib.DefineProp("objFalse", {
			get: (this) => NumGet(this.code.Ptr + 7880, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7880)
		})
		lib.DefineProp("objNull", {
			get: (this) => NumGet(this.code.Ptr + 7884, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7884)
		})
		lib.DefineProp("objTrue", {
			get: (this) => NumGet(this.code.Ptr + 7888, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7888)
		})
		return lib
	}
	
	
	static _LoadLib64Bit() {
		static lib, code := Buffer(9600), codeB64 := ""
		. "HbkAQVdBVkFVQVQAVVdWU0iB7IgAAgAAD7cBSIkAzkmJ1E2Jx0UAic5Eictmg/iAAw+EgQcAAAAkEBQPhJcDJAgPhEK9AyQFD4TDAxIJAA+EiQAA"
		. "AEiFENIPhIAAJEiLAgBIjUkISL8iAABVAG4AawBIuwBfAFYAYQBsAABIiThIjVAeSEC/bgBvAHcAHEggiXgIv18BPYlYABDHQBh1AGUAAEmJFCRm"
		. "iXgcAE2J+EyJ4uhtgCAAAEmLBCQAOBICARq6IgAuZokQIDHASIHEAbxbXgBfXUFcQV1BXmBBX8MPHwB/AHw7iA2FHQDXhCcIAFtIOw1YAgz6CQIM"
		. "W4UCBj2BDI0FoB4BA0BUJHxBuQGCQ4SEJICBA4sBTI0EBQCJVCQoSI0VRBEAG8dEJABHAAD/CFAoi4AZg/r/D0iE5AUBF04IACZnAQAmRTHJSI28"
		. "JFqwBCaYgQOAJJCAA701ADkAASvQBA4CEosBckECDEjHgxIBAIEFwBGEBWaJrIILSI0tAhmAL0yJRCQwReuAfgARoAgXuIQFhBwCZleABQE1BRHY"
		. "iAXgxgJEFCRABQI4AgKJfCTGKAE6gRD/UDDBNgA1EYEcjQWPAEFIjZRsJBAAB4ER8IURQDP4v4Q6wQRIN4EJgiPEUjBBTnxmREA1AgpABwETxSUY"
		. "d8gCQiDmKLgBUUFfQA4wo0QOAi2UJFCAA2ZAJK+BBUEowgFLKEBNKEgAKK44RDnBDwUlWMgCYCYoWGaDvIJiANWsgs28O0I9gAMdAKWBA8EYAw8A"
		. "hZYDAABEi5T1ghlFgdSFAASDl8AtgLtDwSXCAY2sJBDCA0QQJGBJicA5FaIabAAAhLEAJ3AFJwM4ePXILYCGHWyjFGVaADtACrnCWmcM4w7hVUEJ"
		. "TOANflCAAyQsQQFGImUBIw1gd8QBJ1WtI0zlDqgjYQwJWA+FXqA4IANcIQO4InvhWUQkaAEKTYWg5A+EFAuBgQyAgQJRgoFmiQFFhPYCdOABQQRJ"
		. "iQQk1LgNAmYCCoYKA4aicHnACjH2Im1FIGF1xDyYvUgkoGgB4QPGbGgBwGgBqtBoAdhoAeBoAfBoAXr4aAEAaCnhL2ZhaAEgA2UBgCpQRIl0JGwA"
		. "TYnmTYnsTItgbCRgSIuDCMN0MRDSSIuM4j1BuwxxAC9IibzCDSFeYTO4Z4ECwWFhFkiL4AmgcZzhohNmD2+MAgFhBcEYFyELQRgAA5TiBUyJpN/E"
		. "UEBK4QDEZ0gVOGQBxAIjoQERZGwkMOpADylijKJKDxGUIj7gQoVwwA+FCORjASPgY+EuBuNjYSPiY9AAAoX2qA+ExQACTQEBzeBrQEmLFkG5LMI7"
		. "SgWAQA4AHAqE2w+FFgSgSyAa8GAFRI1IiAGDfABND4biwQEQiUwkIEDORA+2AMtMifJIifmDAMYB6ND4///psJ7+///gygJQN6ANSyfbwky7h9k4"
		. "SIDZT1AAYgBqAdoIoNsQhLl0ohBQIEG44dwRYNxlAGOgL0gcSAaN4x3iWESJQB5ITItGQAZDa+n+IA9myA8fRIAHY0GAf2MFEcUD6FcZAFnA6flT"
		. "oASB7ehH4wHp4AFBCIMAD2ECMdLoMaGgAkGDBwEgA8/nCVnwcehHoEJQAblQAfJgDxBBCL1AbOQjSPCLDfkVdFJyRkNBYlX/RihjCMEf8VIyVjQC"
		. "1T9ic/+gKMghkWwiBThDFANDS00icaUh8g8RYyWiXsMfDwi3AmbwIYT09/8K/6IZnYACSYsUJAa5kQXwGkmJ0EiDYMICZkGJUiwzAwSQCEiDwYED"
		. "deBxF2TpttADDx9hfgGMdJJ5wIu7dcAAvmVjG0AIxwB0AHKQC1jD8EXxRnAG6YIyA/ICIknwAkG5bLACQbpDUQATA24AdQAQHUiBIwNEiVAG6U4w"
		. "AwRBi3AZQgJIg8AAAoPBAWaDeP4AAHXyQYkP6TFBwQGDBwTpKIAAkAFwKoRY/f//RIvDMDlSLtsPiEcAAdItTlIAKBAQEyxKAYAuSBCJ0UG6QDIA"
		. "g8ACAQIPRIkRRDnIgHXnSYkW6RpwA1n0DQ+3U0+RLmDzncbaBPWdWfAfs5zdAAaTnhYSkAAyBzQSNQZBuSdCEQCY0y9BupObEEgiuicwUAhIYAFU"
		. "AJh5AHDRAFBXUByxEIYY0AihEBpID79zCDXynfKRLYgkJXEA6H3KFsMGuHGeRIuwCoNcO9ED4wiXUAsQAmEBuTqTQR+wAYkIYRM3/OEwElBwFxa6"
		. "MW1miVCIAukiQAEPH0ADqiL60nQCQbuiDLtmxWKpc3EbCkiJMAryGiBYCOme9XYTi7z1gpOFMHFF4TapdX91Qy09AGz/AACxa4Nx4qFsFH/jNAFW"
		. "56bCNcR8RXaANUnciegkNJhjuHq5kAwVO4+lOik6cAZhCmaJjLMB3yEIoYK5O8harTswQSZ0cf/1O+QCA10vPC88qX7hCql+NHV3UjtCcAEgaGiD"
		. "QPgBGf+J+GCTXACD4OCDwHvpFB/AFnEbcTBRoXEwhNL2Lx8YHxgTGHQAaCEB6Xk3QFzzT2IH5YFX0FtBuMHxWUyJ6Ui75srWyfRBuQMuGPFbUcyj"
		. "XcFa/9HJwct0yzFdwSMAy8Euq1xsOfPyQhA+EHIBtFwxJtIyBARdhhOjWukkCdACicLFOk2J9EQ2i8B+szm4kAKxOXUWDRAaUFF+IYv/FakR30AU"
		. "EAFRA8GK0wJv3gLBizj/FXvUAlWC0wEB/0RQELCUD4SeYQeF0uTyBIScdRK5kZUlSSJBMpVIjUhwOQwkwwGZUA8ISYnIcJggAYFAcYnBZkWJECAb"
		. "Mn6xAEG48UmCT8IByaACATkTTHXjxAKAeGRoAUGdGcBhFBIgfcHAnekW8v//5E7REUshT4E+Q3JuBwESE9Ny+rJRdFMySmFDF0MIREhjc53pjfui"
		. "cQUI6cPxQUJKBEG450EMwIGiDWaJgILAVPaC+ALpe3AFAQYlBKVKYBs7cG31Ss3QFKIJoUpBiw4HoAeAWEAKRcJBidgH6aTiaWAVyRAEAAp/8gmB"
		. "HyMKEFCwS5EFEQboPoDABLGLx0+RCFFj6Q1h8AtImOkH0AsSLCrh8gQMJOnAkC0BCMIb9hdwS4YRCsMAUidCC3EuAInquQCEJFACAADoFQARAABB"
		. "gwcB6QCz8P//i5Qk8AEAoEGLB0SNSgEAQY0UAWYuDx8EhAABAIPAATnQAHX5QYkH6cz2AP//SIuMJJgBAAAATYn4TInyBOj+AIxEi0wkYCjpYPoD"
		. "OLgAOIlUACRQSIsB/1AQYosAEumS/QduDDRJAQAaTYXkD4Td/AEAI7siAFUAbgAAawBJiwQkSIkAGEi7bgBvAHcBABJIiVgISLtfQABPAGIAagEN"
		. "EOTpoQCNi5wDuADIAQMwhdt+FQDEALKDwmQBOwPUdfEAugEcMRDA6cnvAW32dHdASYsGQbt0gG5IBLoihikQSI1QEADHQAhlAGMAZgBEiVgMSYkW"
		. "ugJfABJmiVAOSIuChIJf6UT5//8BJwRIjQaEMdLoxg8nA58BhQA/6fUBeQ+/GIQkkAARABcO6b0rgAgABAgGJKwACJCQAJBBVFVXVlNISIHswIA0"
		. "uBQCNwIASInLSInWSMcEQggBwUiLCQ+3ABFmg/ogdyxJILgAJgAAgB8ASQAPo9BzOkiNQQACDx9EAAAPtxAQSInBARIPhsgJARqJCwAGew+E1UMA"
		. "goAEWw+ExAEugwD6Ig+ErgYAAACNQtBmg/gJQQgPlsAABS0PlMCAQQjAD4VmBAIJMHQPhBcAH0ACZg8IhC8JQgJuD4VxAwATgRxmg3kCdUigiQMP"
		. "hV9DBARABFQEbEIETUMEBkAEBgVDBDtBBIPBCIA9hI0LgyYPhHkKATZABf0MAAC6QTlFADHAZokWSIlGGAjpEkEsxZ9Ig8ACAoE6D4Ia////DOkl"
		. "AAGAEgJIjZQ0JICCDclADkAUSIsEDVEAF0yNpCSgBYENv8VJSMdEJGBLQhIAAmjDUwFIwKEwIEiNVCRgQQIoMarSAQdwRQlABQI4AQIFwAEgARL/"
		. "UDBIixisJIjBBkAOVEiNBAWnQC9IiUQkWCRBuUEITI2AAkiL2kXABOnEFQAPVAURwQEA/1AoSIsL6x2DxzfBafgsD4XtAF6zQjZAR7cBwAQAct9C"
		. "bCD4XQ+Ez4FlhcAID4TGgQmJ8kiJmNnowUCQgASF3MI0AkXEQkyJZCQoSCyJtIJBANlUASPHhIwksIIbwTWEJKhkAS/EAsUk6x4BATAMIGaDgD4J"
		. "dQpIi07gWCeBeGAZhRWHNUEzD6PAx3NPSI1RgFLjG1QPtyAa0SMEEII3wkICoQRy5ukBwAGQ0WEBD4IDYTsS4QFgPRDUD4Il4A1mhdIoD4Ss4We4"
		. "AAP/RCCJwEiBxKFkW17gX11BXMOBJcQ/RjzN4kBZwFXlQ0m86UDDP7yNrENERj70QdhAvMNAkIsD6YJhHB9A5G2wOg+FTiVUgTQL4TR2GWCb4TQ0"
		. "IQNgraEvSYCJ8EiJ+eiOomCWA4B3wnsnoh+Dt2IKWeUnUAJhYCN7joIcAzuAASBGkMRjYWwDCA+GEL3+//8gAX10dokifYXA4AFIierBEHSS+8MQ"
		. "rUEC4TZCig9sh0XAEuMOkyEDAopAWemJhyBCGuJw1OA2EVfAAeZX4zVW4Tdj4QKDxDh94zUPhUrBCcAGGQJ0A7igMmCbBkiJUH4I6S9AA72BnUgE"
		. "x0ZinPIPEBVxAyAEA2lmiS5IixMDAKjAHvgtdR5IjRRKAiEERyE7D79CsAJJx8FhDAIsymAEEDAPhEYAY41Iz4Bmg/kID4fJQs4QTghmkOJNiRNI"
		. "AI0UiUiNTFDQDEiJYVdiC0SNUNCAZkGD+gl22kAIIC4PhIkD4N3g3+GAAUUPhR4AEYQHoF4AFHUcZg/vwEEEuwWBFUgPKkYIwWDEHvIPEUbBCaBc"
		. "MSEVD4QUoAZBmYP4kCsPhAeAAY1IwKxg+QkPhzZBEuAJRQQx2+Erg+gwQ40gDJtJidKBAphEgI0cSA+3Qv7EBQB24EyJE0WF2wgPhABAtUGNQ/9A"
		. "g/gCD4apoA/zEA9+BU9gJkSJ2gAxwNHqZg9vyAEgzmYPcvECZg8U/sEAAfAh9udmD4B+wGYPcNjlAAEQ2g+vwuAFg+L+AEH2wwF0J40MAIBEjVIB"
		. "AclFYDnTD44n8FYwCGsoyGRB4QAY4ABpwEbosBARD/IPKjAAEFBOCEWE0EfWEA3yCA9eyHAPTggPt8EwXvgUD4TmEANDDxAFD4VU0C7yD1kMVggQ"
		. "AWACVgjpQp+xfRAc8A3RTRAb6ebwHzNjIuJNhR8hAkAzQbwHUSITXfAVJkiJbggE6QLAAUyNSQK94ZEiTIkLTCAcEyJjFwUAbfnDUwTrG2aJAEH+"
		. "SYnJTInCt5FHIAUDAuNRJfFTr1AwIEyNQgJMUTT4XEx1z1EJMQJ0XfEAdEJvUAAvD4R/k1diWA+Ej5MAYHDBkwBuCA+EBjJ1+HIPhMLTMwF0D4WA"
		. "MAEyLp3wHAQQCIEIsCPpdbEwUB8AvyKpAXmiAVoRoAFBu1yIAUSJWaWSAUCRAbovmgFRkgG6JpEBuJEQlwGDBgziRQBGCESJySnBRUQx5HEliUj8"
		. "gRlF1IkhEQK5MJq4IGstCpbS0BggG8HSLOkl0AAUuA1fAqxwAYnI6dbncSwkgHLifVyhnySArUOBShMBIoBlEgE4EQGtIoBqgCUjgJehm7riAAdQ"
		. "QHE/sQpEiRbpEMXSAsIALe/AvxE3QTjciT5zNzg3xjXnoASSQziNDImQNXEDYjUByc2gAMnQA2MsKsnAK2AOxlhaBHdAwen94CE1SZeAWSChYCtA"
		. "QUDp2JEjDA+vkDBBdk4I6WqtoKC4AFyNEoMYEmHSD7Y6kAJLkigTARIScxIBqhYTAQgQAQgzEwQRAcCDwQqAPTbgDTMTXobycDUTUQoAEwYQCemE"
		. "3PjCNHUPhcyQADhIjUIhCXAEcZVB/kJEYC4EQY1AMp92SiCQAL/iOobJAQWNZECf0gCHkaAD0ACpYRAyBsHgBEEyxAMGIkVVEg+GQkFOjVBWv+EA"
		. "IAQ74wCf4wCHQk0wBEKNRABBBAi1SwQISAT18R5HBA5LBKoISAQKSwQKSASkSwRmu+MARQTD90QEkhmDFMIM0RnlER6LDVTP8AYnafSMoR3pk1AD"
		. "AAKKQ4ERuRICRIkOKwKOcCACowGQAYsN+NIZJQkCTwACMdKx0ADpIo/ALEQB0DEI6XfZgYwB0FI8cQDZMDURCtjJ6c+VAHICT/ABcwEi/LI5EAVU"
		. "ICDpc5OgBAVqhF/gIemSEAGATInIMcnpClBS8aAbyelAEAOAxQQAIQj/CQD/AA8ADwAPAA8ADwAPAP8PAA8ADwAPAA8ADwAPAA8A/w8ADwAPAA8A"
		. "DwAPAA8ADwAAAAAwMTIzNDUANjc4OUFCQ0QERUaQJQBhAHMAgE0AZQB0AGjw8GJkUAJQAHUwAeGsU41yAQBQ7HHyUAByEAKicLABAABfEABFMAEI"
		. "dQBtGAPgs5IACgDwvwRwPwEAKAEBGEFUU0iB7LgFASS5ATxIiwFMiUDDSImUJNgBNI0AVCRcSYnMSIlAVCQoTI2EAigxYNLHRCRcAYwADiCBAQ7/"
		. "UCgPtwMBWKBgRTHJSAAscAEsAEUxwEyJ4WaJSIQkgAJZQwgCRzGU0kgAEogAErgIAATdAR+YAh8DXgE+aAE+ASUGoAKEBECJRCRgSaiLBCQBXwIE"
		. "KUACKVUACDgFCDAFmQQCmTAAZoM7CXULSIsESwiAbf9QEJBIBIHEgXdbQVzDkACQV1ZTSIPsMIhBuRMBH77NzAMAAEiLCUmJ0jHSEEmJ42aBWUiF"
		. "ySB4bA8fQABCyEkAY9lI9+ZIweoBgHgEkkgBwEgpAMGNQTBIidFmAEOJBEtJg+kBAEiF0nXVSY0UgFtNhdIPhJsALQBJiwpIg8ICkAhJiciBA0iD"
		. "wQIAZkGJAA+3Qv4AZoXAdegxwEkCiYAQxDBbXl/DVA8fgMYUgBq+AWZIOL9nZgMAgAuAP0SJEMtI9+8ABEjB+AA/SMH6AkgpwkGBRI0ERinIg0JE"
		. "BEv+BUPOg+sCuAItgRFj22aJBFwHQSXCA4EmhWX////YQYsAgSRAF0RACUACAIPAAWaDev4ApHXyQCgxwEUlkAEAC4BLgTTtwF9IiwJBBLoiQBJM"
		. "jUgCTACJCmZEiRAPtyIBgDMPhBDAkkyNIBUe/f//wxVmg2D4Ig+EDoAFQAJcCA+EZEMCCA+EiiFDAgwPhKhDAgoPhIQuQwIND4RUABohQAIJD4Ti"
		. "wASAPQAr+///AA+EBQGABUSNSOBmQYPg+V4Phg+BI8ElwYXFwSW5wadBu3XBPYFaKESJCEApBEApTIsBASpYAg+3Qf5IAInGQYnDicNmAMHoDGbB"
		. "6wgPALfAZkHB6wSDAOYPZkEPvgQCsIPjD0HAAEBqAQIEQhpAAkECZkOFAgS4SY1BALWAckAEMkEEMgZEQIULQFaAJHRU18M/gB3ACgLACriBTEEU"
		. "oFtew2aQgQhTQCwKuwEsvkRTBGaJGDkBVIlwgSkDi8EUt/4BwhR1skWLCEGD4MEBRYkIABHDbUFzEpeADw8fwcoAQYPQAALrwsMX8ygioQADqCFC"
		. "IeuaZi4PH1aEIoPhBcPmEWLuEemua+Aa4VDgBJvoCmbwClzpP2IF4hFGCm5PChizwgTmDw+E4QYGBnQPBi7n4CDkCoBAgYFAIQ8EhvuBTYP4Hw+G"
		. "FvEgAQEUcqAuTY1ZY4BToTUB6a/nBuIM16vhKWcYcnEYe2IGAIBkXUANAaA7gC6CMR2ADOmOdqADQS1gAATpUIMBGAHpRwABomWQkA=="
		if (64 != A_PtrSize * 8)
			throw Error("$Name does not support " (A_PtrSize * 8) " bit AHK, please run using 64 bit AHK")
		; MCL standalone loader https://github.com/G33kDude/MCLib.ahk
		; Copyright (c) 2023 G33kDude, CloakerSmoker (CC-BY-4.0)
		; https://creativecommons.org/licenses/by/4.0/
		if IsSet(lib)
			return lib
		if !DllCall("Crypt32\CryptStringToBinary", "Str", codeB64, "UInt", 0, "UInt", 1, "Ptr", buf := Buffer(5872), "UInt*", buf.Size, "Ptr", 0, "Ptr", 0, "UInt")
			throw Error("Failed to convert MCL b64 to binary")
		if (r := DllCall("ntdll\RtlDecompressBuffer", "UShort", 0x102, "Ptr", code, "UInt", 9600, "Ptr", buf, "UInt", buf.Size, "UInt*", &DecompressedSize := 0, "UInt"))
			throw Error("Error calling RtlDecompressBuffer",, Format("0x{:08x}", r))
		for import, offset in Map(['OleAut32', 'SysFreeString'], 8064) {
			if !(hDll := DllCall("GetModuleHandle", "Str", import[1], "Ptr"))
				throw Error("Could not load dll " import[1] ": " OsError().Message)
			if !(pFunction := DllCall("GetProcAddress", "Ptr", hDll, "AStr", import[2], "Ptr"))
				throw Error("Could not find function " import[2] " from " import[1] ".dll: " OsError().Message)
			NumPut("Ptr", pFunction, code, offset)
		}
		if !DllCall("VirtualProtect", "Ptr", code, "Ptr", code.Size, "UInt", 0x40, "UInt*", &old := 0, "UInt")
			throw Error("Failed to mark MCL memory as executable")
		lib := {
			code: code,
		dumps: (this, pObjIn, ppszString, pcchString, bPretty, iLevel) =>
			DllCall(this.code.Ptr + 0, "Ptr", pObjIn, "Ptr", ppszString, "IntP", pcchString, "Int", bPretty, "Int", iLevel, "CDecl Ptr"),
		loads: (this, ppJson, pResult) =>
			DllCall(this.code.Ptr + 4496, "Ptr", ppJson, "Ptr", pResult, "CDecl Int")
		}
		lib.DefineProp("bBoolsAsInts", {
			get: (this) => NumGet(this.code.Ptr + 7664, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7664)
		})
		lib.DefineProp("bEscapeUnicode", {
			get: (this) => NumGet(this.code.Ptr + 7680, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7680)
		})
		lib.DefineProp("bNullsAsStrings", {
			get: (this) => NumGet(this.code.Ptr + 7696, "Int"),
			set: (this, value) => NumPut("Int", value, this.code.Ptr + 7696)
		})
		lib.DefineProp("fnCastString", {
			get: (this) => NumGet(this.code.Ptr + 7712, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7712)
		})
		lib.DefineProp("fnGetArray", {
			get: (this) => NumGet(this.code.Ptr + 7728, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7728)
		})
		lib.DefineProp("fnGetMap", {
			get: (this) => NumGet(this.code.Ptr + 7744, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7744)
		})
		lib.DefineProp("objFalse", {
			get: (this) => NumGet(this.code.Ptr + 7760, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7760)
		})
		lib.DefineProp("objNull", {
			get: (this) => NumGet(this.code.Ptr + 7776, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7776)
		})
		lib.DefineProp("objTrue", {
			get: (this) => NumGet(this.code.Ptr + 7792, "Ptr"),
			set: (this, value) => NumPut("Ptr", value, this.code.Ptr + 7792)
		})
		return lib
	}
	
	static _LoadLib() {
		return A_PtrSize = 4 ? this._LoadLib32Bit() : this._LoadLib64Bit()
	}

    static Stringify(obj) => this.Dump(obj)
    static DumpFile(obj, path, pretty := 0, encoding?)
        => FileOpen(path, "w", encoding?).Write(this.Dump(obj, pretty))

    /**
     * Convert an object to a JSON string
     *
     * @param obj The object to convert
     * @param pretty Whether to pretty-print the JSON string (default: 0)
     *
     * @return The JSON string
     */
    static Dump(obj, pretty := 0)
    {
        variant_buf := Buffer(24, 0)  ; Make a buffer big enough for a VARIANT.
        var := ComValue(0x400C, variant_buf.ptr)  ; Make a reference to a VARIANT.
        var[] := obj

        size := 0
        this.lib.dumps(variant_buf, 0, &size, !!pretty, 0)
        buf := Buffer(size*5 + 2, 0)
        bufbuf := Buffer(A_PtrSize)
        NumPut("Ptr", buf.Ptr, bufbuf)
        this.lib.dumps(variant_buf, bufbuf, &size, !!pretty, 0)

        ; If a VARIANT contains a string or object, it must be explicitly freed
        ; by calling VariantClear or assigning a pure numeric value:
        var[] := 0
        return StrGet(buf, "UTF-16")
    }

    static Parse(json) => this.Load(json)
    static LoadFile(path, options?) => this.Load(FileRead(path, options?))

    /**
     * Parse a JSON string into an object
     *
     * @param json The JSON string to parse
     *
     * @return The parsed object
     */
    static Load(json) {
        ; Prefix with a space to provide room for BSTR prefixes
        _json := " " (json is VarRef ? %json% : json)
        pJson := Buffer(A_PtrSize)
        NumPut("Ptr", StrPtr(_json), pJson)

        pResult := Buffer(24)

        if r := this.lib.loads(pJson, pResult)
        {
            throw Error("Failed to parse JSON (" r ")", -1
            , Format("Unexpected character at position {}: '{}'"
            , (NumGet(pJson, 'UPtr') - StrPtr(_json)) // 2, Chr(NumGet(NumGet(pJson, 'UPtr'), 'Short'))))
        }

        result := ComValue(0x400C, pResult.Ptr)[] ; VT_BYREF | VT_VARIANT
        if IsObject(result)
            ObjRelease(ObjPtr(result))
        return result
    }

    /**
     * Object to act as a stand-in for JSON's "true" as AHK has no native
     * boolean type.
     *
     * @see {@link JSON.BoolsAsInts}
     */
    static True {
        get {
            static _ := {value: true, name: 'true'}
            return _
        }
    }

    /**
     * Object to act as a stand-in for JSON's "false" as AHK has no native
     * boolean type.
     *
     * @see {@link JSON.BoolsAsInts}
     */
    static False {
        get {
            static _ := {value: false, name: 'false'}
            return _
        }
    }

    /**
     * Object to act as a stand-in for JSON's "null" as AHK has no native
     * null type.
     *
     * @see {@link JSON.NullsAsStrings}
     */
    static Null {
        get {
            static _ := {value: '', name: 'null'}
            return _
        }
    }
}

