package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Statified.mediaPlayer
import com.vivekcorp.echoapplication.model.AudioModel
import com.vivekcorp.echoapplication.model.CurrentSongHelper
import java.util.*
import kotlin.properties.Delegates

class NowPlayingFragment : Fragment() {

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    lateinit var audioVisualization: AudioVisualization
    lateinit var glView: GLAudioVisualizationView

    lateinit var seekBar: SeekBar
    lateinit var startTimeText: TextView
    lateinit var endTimeText: TextView

    lateinit var playPauseImageButton: ImageButton
    lateinit var previousImageButton: ImageButton
    lateinit var nextImageButton: ImageButton
    lateinit var loopImageButton: ImageButton
    lateinit var shuffleImageButton: ImageButton
    lateinit var songArtistView: TextView
    lateinit var currentSongHelper: CurrentSongHelper
    lateinit var songTitleView: TextView
    lateinit var fab: ImageButton
    lateinit var fetchSongs: ArrayList<AudioModel>
    var currentPosition: Int = 0
    var flag by Delegates.notNull<Boolean>()
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sharedPreferences =
                activity?.getSharedPreferences(
                    getString(R.string.preferences_file_name),
                    Context.MODE_PRIVATE
                )!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        setHasOptionsMenu(true)

        activity?.title = "Now Playing"
        seekBar = view.findViewById(R.id.seekBar)
        startTimeText = view.findViewById(R.id.startTime)
        endTimeText = view.findViewById(R.id.endTime)
        playPauseImageButton = view.findViewById(R.id.playPauseButton)
        nextImageButton = view.findViewById(R.id.nextButton)
        previousImageButton = view.findViewById(R.id.previousButton)
        loopImageButton = view.findViewById(R.id.loopButton)
        shuffleImageButton = view.findViewById(R.id.shuffleButton)
        songArtistView = view.findViewById(R.id.songArtist)
        songTitleView = view.findViewById(R.id.songTitle)
        glView = view.findViewById(R.id.visualizer_view)
        fab = view.findViewById(R.id.favouriteIcon)
        glView = view.findViewById(R.id.visualizer_view)
        fab.alpha = 0.8f

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_redirect -> {
                println("here")
                activity?.onBackPressed()
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        currentSongHelper = CurrentSongHelper()
        currentSongHelper.isPlaying = true

        if (currentSongHelper.isPlaying) {
            playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
        }

        var path: String? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songId: Long? = null

        try {
            path = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")!!.toLong()

            currentPosition = arguments!!.getInt("position") + 1

            fetchSongs = arguments?.getParcelableArrayList("songData")!!

            currentSongHelper.songPath = path
            currentSongHelper.songTitle = songTitle
            currentSongHelper.songArtist = songArtist
            currentSongHelper.songId = songId
            currentSongHelper.currentPosition = currentPosition

            println(currentSongHelper.songTitle)

            updateTextViews(
                currentSongHelper.songTitle as String,
                currentSongHelper.songArtist as String
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        try {
            mediaPlayer!!.setDataSource(activity as Context, Uri.parse(path))
            mediaPlayer!!.prepare()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer!!.start()

        clickHandler()

        val visualizationHandler = DbmHandler.Factory.newVisualizerHandler(activity as Context, 0)

        audioVisualization.linkTo(visualizationHandler)
    }

    fun playPrevious() {

        println(currentPosition)
        currentPosition -= 1
        if (currentPosition == -1) {
            println(currentPosition)
            currentPosition = fetchSongs.size - 1
        }
        if (currentSongHelper.isPlaying as Boolean) {
            playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper.isLoop = false

        val nextSong = fetchSongs[currentPosition]
        currentSongHelper.songPath = nextSong.songData
        currentSongHelper.songTitle = nextSong.songTitle
        currentSongHelper.songArtist = nextSong.artist
        currentSongHelper.songId = nextSong.songID
        updateTextViews(
            currentSongHelper.songTitle as String,
            currentSongHelper.songArtist as String
        )
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(activity as Context, Uri.parse(currentSongHelper.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
//            processInformation(Statified.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        if (favouriteContent?.checkifIDExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
//            fab?.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.favorite_on))
//        } else {
//            fab?.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.favorite_off))
//        }
    }

    private fun clickHandler() {

        //play pause event
        playPauseImageButton.setOnClickListener {
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer!!.pause()
                currentSongHelper.isPlaying = false
                playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaPlayer!!.start()
                currentSongHelper.isPlaying = true
                playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
            }
        }

        nextImageButton.setOnClickListener {
            currentSongHelper.isPlaying = true
            playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
            if (currentSongHelper.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        }

        previousImageButton.setOnClickListener {
            currentSongHelper.isPlaying = true
            if (currentSongHelper.isLoop) {
                loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }
    }

    fun playNext(check: String) {

        if (check.equals("PlayNextNormal", true)) {
            currentPosition += 1
        } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
            var randomObject = Random()
            var randomPosition = randomObject.nextInt(fetchSongs.size.plus(1) as Int)
            currentPosition = randomPosition
        }
        if (currentPosition == fetchSongs.size) {
            currentPosition = 0
        }

        currentSongHelper.isLoop = false
        var nextSong = fetchSongs[currentPosition]
        currentSongHelper.songPath = nextSong.songData
        currentSongHelper.songTitle = nextSong.songTitle
        currentSongHelper.songArtist = nextSong.artist
        currentSongHelper.songId = nextSong.songID as Long
        updateTextViews(
            currentSongHelper.songTitle as String,
            currentSongHelper.songArtist as String
        )
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(activity as Activity, Uri.parse(currentSongHelper.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            mediaPlayer
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        if (favouriteContent.checkifIDExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
//            fab.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.favorite_on))
//        } else {
//            fab.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.favorite_off))
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView
    }

    override fun onResume() {
        super.onResume()
        audioVisualization.onResume()
    }

    override fun onPause() {
        super.onPause()
        audioVisualization.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioVisualization.release()
    }

    fun updateTextViews(songTitle: String, songArtist: String) {
        var songTitleUpdated = songTitle
        var songArtistUpdated = songArtist
        if (songTitle.equals("<unknown>", true)) {
            songTitleUpdated = "unknown"
        }
        if (songArtist.equals("<unknown>", true)) {
            songArtistUpdated = "unknown"
        }
        songTitleView.text = songTitleUpdated
        songArtistView.text = songArtistUpdated
    }


}
