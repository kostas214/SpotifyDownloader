from pytube import YouTube
import spotipy
from spotipy.oauth2 import SpotifyClientCredentials
import uyts
import string
import music_tag
import requests




def songSearchSpotify(playlistLink):
    #Initialize Spotipy
    SPOTIPY_CLIENT_ID = "69b85ad4ba7d4efb9265321b98a775fd"
    SPOTIPY_CLIENT_SECRET = "247f5c4dd5334e94a2aa7b67c1a53366"
    sp = spotipy.Spotify(client_credentials_manager=SpotifyClientCredentials(client_id=SPOTIPY_CLIENT_ID,
                                                                             client_secret=SPOTIPY_CLIENT_SECRET,
                                                                             ))

    #Store the songs of the playlist in a list
    done = True
    offset = 0
    songs = []
    links = []
    tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
    for key in tracks['items']:
        songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
        links.append(key['track']['external_urls']['spotify'])
    while done:
        if len(songs) == offset + 100:
            tracks = sp.playlist_items(playlist_id=playlistLink, offset=offset)
            for key in tracks['items']:
                songs.append(f"{key['track']['name']} {key['track']['artists'][0]['name']}")
            offset += 100
        if len(songs) < offset + 100:
            done = False

    return songs,links





def DownloadSongs(songs,links,filePath):
    SPOTIPY_CLIENT_ID = "69b85ad4ba7d4efb9265321b98a775fd"
    SPOTIPY_CLIENT_SECRET = "247f5c4dd5334e94a2aa7b67c1a53366"
    sp = spotipy.Spotify(client_credentials_manager=SpotifyClientCredentials(client_id=SPOTIPY_CLIENT_ID,
                                                                             client_secret=SPOTIPY_CLIENT_SECRET,
                                                                             ))
    songId = uyts.Search(songs)
    result = "https://www.youtube.com/watch?v=" + songId.results[0].id
    translation_table = str.maketrans('', '', string.punctuation)
    safeString = songId.results[0].title
    safeString = safeString.translate(translation_table)
    fileName = safeString.replace(" ", "") + ".aac"

    tracks = sp.track(track_id=links)

    ArtWorkURL = tracks['album']['images'][0]['url']
    albumName = tracks['album']['name']
    albumArtistName = tracks['album']['artists'][0]['name']
    albumTrackCount = tracks['album']['total_tracks']
    albumTrackNumber = tracks['track_number']
    releaseDate = tracks['album']['release_date'][0:4]
    artistName = tracks['artists'][0]['name']
    trackName = tracks['name']

    response = requests.get(ArtWorkURL)

    yt = YouTube(result)
    ys = yt.streams.get_audio_only()
    ys.download(filePath, filename=fileName, timeout=30)








    f = music_tag.load_file(f"{filePath}/{fileName}")
    f['album'] = albumName
    f['albumartist'] = albumArtistName
    f['artist'] = artistName


    f['artwork'] = response.content
    f['totaltracks'] = albumTrackCount
    f['tracknumber'] = albumTrackNumber
    f['tracktitle'] = trackName
    f['year'] = releaseDate
    f.save()





