package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.room.Room
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.vivekcorp.echoapplication.R
import com.vivekcorp.echoapplication.database.Entities
import com.vivekcorp.echoapplication.database.SongDatabase
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Statified.mediaPlayer
import com.vivekcorp.echoapplication.model.AudioModel
import com.vivekcorp.echoapplication.model.CurrentSongHelper
import java.util.*
import java.util.concurrent.TimeUnit

class NowPlayingFragment : Fragment() {

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    var MY_PREFS_SHUFFLE = "Shuffle Feature"
    var MY_PREFS_LOOP = "Loop Feature"

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
    var currentPosition = 0

    lateinit var songEntities: Entities

    var updateSongTime = object : Runnable {
        override fun run() {
            val getCurrent = mediaPlayer?.currentPosition
            val minute = TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)
            var second = TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()))

            if ((second % 60).toInt() >= 0){
                second %= 60
            }

            startTimeText.text = String.format(
                "%d:%d",
                minute,
                second
            )
            seekBar.progress = getCurrent
            Handler().postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
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
        var date: Long
        var frag: String

        try {

            path = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")!!.toLong()
            date = arguments?.getLong("songDate")!!
            frag = arguments!!.getString("fragment").toString()

            if (frag == "FavoritesFragment"){

            }

            val callback = object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(callback)

            currentPosition = arguments!!.getInt("songPosition")

            fetchSongs = arguments?.getParcelableArrayList("songData")!!

            currentSongHelper.songPath = path
            currentSongHelper.songTitle = songTitle
            currentSongHelper.songArtist = songArtist
            currentSongHelper.songId = songId
            currentSongHelper.currentPosition = currentPosition

            songEntities = Entities(
                songId.toLong(),
                songTitle.toString(),
                songArtist.toString(),
                path.toString(),
                currentPosition,
                date
            )

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
        processInformation(mediaPlayer as MediaPlayer)

        if (currentSongHelper.isPlaying as Boolean) {
            playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
        }
        mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }

        mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }

        clickHandler()

        val visualizationHandler = DbmHandler.Factory.newVisualizerHandler(activity as Context, 0)

        audioVisualization.linkTo(visualizationHandler)
    }

    private fun processInformation(mediaPlayer: MediaPlayer) {

        var finalTime = mediaPlayer.duration
        var startTime = mediaPlayer.currentPosition

        seekBar.max = finalTime
        startTimeText.text = String.format(
            "%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
        )
        endTimeText.text = String.format(
            "%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))
        )
        Handler().postDelayed(updateSongTime, 1000)
        seekBar.progress = startTime

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {

                val getCurrent = Statified.mediaPlayer?.currentPosition
                val minute = TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long).toString()
                var second = (TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))
                var secondString = second.toString()
                if ((second % 60).toInt() >= 0){
                    second %= 60
                    if (second < 10){
                        secondString = "0$second"
                    }else{
                        secondString = "$second"
                    }
                }else{
                    secondString = "$second"
                }

                startTimeText.text = String.format(
                    "%s:%s",
                    minute,
                    secondString
                )
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {

                mediaPlayer.seekTo(seek.progress)
            }
        })


    }

    private fun playPrevious() {

        currentPosition -= 1
        if (currentPosition == -1) {
            currentPosition = fetchSongs.size - 1
        }
        if (currentSongHelper.isPlaying) {
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
            processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        loopImageButton.setOnClickListener {
            var editorShuffle =activity?.getSharedPreferences(
                MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop =  activity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper.isLoop) {
                currentSongHelper.isLoop = false
                loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            } else { currentSongHelper.isLoop = true
                currentSongHelper.isShuffle = false
                loopImageButton.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", true)
                editorLoop?.apply()
            }
        }

        shuffleImageButton.setOnClickListener {

            var editorShuffle = activity?.getSharedPreferences(
                MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()

            var editorLoop = activity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper.isShuffle as Boolean) {
                shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper.isShuffle = false
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper.isShuffle = true
                currentSongHelper.isLoop = false
                shuffleImageButton.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("Feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            }
        }

        fab.setOnClickListener {
            if (!DBAsyncTask(activity as Context, songEntities, 1).execute()
                    .get()
            ) {
                val async =
                    DBAsyncTask(activity as Context, songEntities, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        context,
                        "song added to favourites",
                        Toast.LENGTH_SHORT
                    ).show()

                    fab.setImageResource(R.drawable.favorite_on)
                } else {
                    Toast.makeText(
                        context,
                        "Some error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()

                    fab.setImageResource(R.drawable.favorite_off)
                }
            } else {
                val async =
                    DBAsyncTask(activity as Context, songEntities, 3).execute()
                val result = async.get()

                if (result) {
                    Toast.makeText(
                        context,
                        "song removed from favorites",
                        Toast.LENGTH_LONG
                    ).show()

                    fab.setImageResource(R.drawable.favorite_off)
                } else {
                    Toast.makeText(
                        context,
                        "Some error occured",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun onSongComplete() {
        if (currentSongHelper.isShuffle) {
            playNext("PlayNextLikeNormalShuffle")
            currentSongHelper.isPlaying = true
        } else {
            if (currentSongHelper.isLoop as Boolean) {
                currentSongHelper.isPlaying = true
                val nextSong = fetchSongs[currentPosition]
                currentSongHelper.songTitle = nextSong.songTitle
                currentSongHelper.songPath = nextSong.songData
                currentSongHelper.currentPosition = currentPosition
                currentSongHelper.songId = nextSong.songID as Long
                updateTextViews(
                    currentSongHelper.songTitle as String,
                    currentSongHelper.songArtist as String
                )
                mediaPlayer?.reset()
                try {
                    mediaPlayer?.setDataSource(
                        activity as Context,
                        Uri.parse(currentSongHelper.songPath)
                    )
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    processInformation(mediaPlayer as MediaPlayer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                playNext("PlayNextNormal")
                currentSongHelper.isPlaying = true
            }
        }
    }

    private fun playNext(check: String) {

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
            processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun updateTextViews(songTitle: String, songArtist: String) {
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

    class DBAsyncTask(
        val context: Context,
        private val songEntity: Entities,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        /*
            Mode 1 -> Check DB if the book is favourite or not
            Mode 2 -> Save the book into DB as favourite
            Mode 3 -> Remove the favourite book
             */

        val db = Room.databaseBuilder(context, SongDatabase::class.java, "songsNew-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
//                    Mode 1 -> Check DB if the book is favourite or not
                    val song: Entities = db.songDao().getSongById(songEntity.song_id.toString())
                    db.close()
                    return song != null
                }
                2 -> {
//                    Mode 2 -> Save the book into DB as favourite
                    db.songDao().insertSong(songEntity)
                    db.close()
                    return true
                }
                3 -> {
//                    Mode 3 -> Remove the favourite book
                    db.songDao().deleteSong(songEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

}
