package com.vivekcorp.echoapplication.fragment

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.MY_PREFS_LOOP
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.MY_PREFS_SHUFFLE
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.onSongComplete
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.playNext
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.playPrevious
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Staticated.processInformation
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Statified.mediaPlayer
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Statified.myActivity
import com.vivekcorp.echoapplication.fragment.NowPlayingFragment.Statified.songEntities
import com.vivekcorp.echoapplication.fragment.SettingsFragment.Statified.MY_PREFS_NAME
import com.vivekcorp.echoapplication.model.AudioModel
import com.vivekcorp.echoapplication.model.CurrentSongHelper
import java.util.*
import java.util.concurrent.TimeUnit

class NowPlayingFragment : Fragment() {

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    object Statified{
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        lateinit var audioVisualization: AudioVisualization
        lateinit var glView: GLAudioVisualizationView

        lateinit var seekBar: SeekBar
        lateinit var startTimeText: TextView
        lateinit var endTimeText: TextView

        var MY_PREFS_NAME = "ShakeFeature"
        lateinit var playPauseImageButton: ImageButton
        lateinit var previousImageButton: ImageButton
        lateinit var nextImageButton: ImageButton
        lateinit var loopImageButton: ImageButton
        lateinit var shuffleImageButton: ImageButton
        lateinit var songArtistView: TextView
        lateinit var currentSongHelper: CurrentSongHelper
        lateinit var songTitleView: TextView
        lateinit var fab: ImageButton
        var mSensorManager: SensorManager? = null
        lateinit var fetchSongs: ArrayList<AudioModel>
        var currentPosition = 0

        var mSensorListener: SensorEventListener? = null

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

                Statified.startTimeText.text = String.format(
                    "%d:%d",
                    minute,
                    second
                )
                Statified.seekBar.progress = getCurrent
                Handler().postDelayed(this, 1000)
            }
        }
    }

    object Staticated {

        var MY_PREFS_SHUFFLE = "Shuffle Feature"
        var MY_PREFS_LOOP = "Loop Feature"

        fun onSongComplete() {
            if (Statified.currentSongHelper.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper.isPlaying = true
            } else {
                if (Statified.currentSongHelper.isLoop as Boolean) {
                    Statified.currentSongHelper.isPlaying = true

                    val nextSong = Statified.fetchSongs[Statified.currentPosition]
                    Statified.currentSongHelper.songTitle = nextSong.songTitle
                    Statified.currentSongHelper.songPath = nextSong.songData
                    Statified.currentSongHelper.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper.songId = nextSong.songID as Long
                    updateTextViews(
                        Statified.currentSongHelper.songTitle as String,
                        Statified.currentSongHelper.songArtist as String
                    )
                    mediaPlayer?.reset()
                    try {
                        mediaPlayer?.setDataSource(
                            myActivity as Context,
                            Uri.parse(Statified.currentSongHelper.songPath)
                        )
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        processInformation(mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper.isPlaying = true
                }
            }
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
            Statified.songTitleView.text = songTitleUpdated
            Statified.songArtistView.text = songArtistUpdated
        }

        fun playNext(check: String) {

            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition += 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs.size.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if (Statified.currentPosition == Statified.fetchSongs.size) {
                Statified.currentPosition = 0
            }

            Statified.currentSongHelper.isLoop = false
            var nextSong = Statified.fetchSongs[Statified.currentPosition]
            Statified.currentSongHelper.songPath = nextSong.songData
            Statified.currentSongHelper.songTitle = nextSong.songTitle
            Statified.currentSongHelper.songArtist = nextSong.artist
            Statified.currentSongHelper.songId = nextSong.songID as Long

            updateTextViews(
                Statified.currentSongHelper.songTitle as String,
                Statified.currentSongHelper.songArtist as String
            )
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(Statified.currentSongHelper.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            songEntities = Entities(
                nextSong.songID,
                nextSong.songTitle,
                nextSong.artist,
                nextSong.songData,
                Statified.currentPosition,
                nextSong.dateAdded
            )

            val checkFav = DBAsyncTask(myActivity as Context, songEntities, 1).execute()
            val isFav = checkFav.get()

            if (isFav) {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))

            } else {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
            }
        }

        fun processInformation(mediaPlayer: MediaPlayer) {

            var finalTime = mediaPlayer.duration
            var startTime = mediaPlayer.currentPosition

            Statified.seekBar.max = finalTime
            Statified.startTimeText.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
            )
            Statified.endTimeText.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))
            )
            Handler().postDelayed(Statified.updateSongTime, 1000)
            Statified.seekBar.progress = startTime

            Statified.seekBar.setOnSeekBarChangeListener(object :
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

                    Statified.startTimeText.text = String.format(
                        "%s:%s",
                        minute,
                        secondString
                    )
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
                    mediaPlayer.seekTo(seek.progress)
                }
            })
        }

        fun playPrevious() {

            Statified.currentPosition -= 1
            if (Statified.currentPosition == -1) {
                Statified.currentPosition = Statified.fetchSongs.size - 1
            }
            if (Statified.currentSongHelper.isPlaying) {
                Statified.playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
            } else {
                Statified.playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
            }
            Statified.currentSongHelper.isLoop = false

            val nextSong = Statified.fetchSongs[Statified.currentPosition]
            Statified.currentSongHelper.songPath = nextSong.songData
            Statified.currentSongHelper.songTitle = nextSong.songTitle
            Statified.currentSongHelper.songArtist = nextSong.artist
            Statified.currentSongHelper.songId = nextSong.songID
            updateTextViews(
                Statified.currentSongHelper.songTitle as String,
                Statified.currentSongHelper.songArtist as String
            )
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(Statified.currentSongHelper.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            songEntities = Entities(
                nextSong.songID,
                nextSong.songTitle,
                nextSong.artist,
                nextSong.songData,
                Statified.currentPosition,
                nextSong.dateAdded
            )

            val checkFav = DBAsyncTask(myActivity as Context, songEntities, 1).execute()
            val isFav = checkFav.get()

            if (isFav) {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))

            } else {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)

        setHasOptionsMenu(true)

        activity?.title = "Now Playing"
        Statified.seekBar = view.findViewById(R.id.seekBar)
        Statified.startTimeText = view.findViewById(R.id.startTime)
        Statified.endTimeText = view.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view.findViewById(R.id.nextButton)
        Statified.previousImageButton = view.findViewById(R.id.previousButton)
        Statified.loopImageButton = view.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view.findViewById(R.id.songArtist)
        Statified.songTitleView = view.findViewById(R.id.songTitle)
        Statified.glView = view.findViewById(R.id.visualizer_view)
        Statified.fab = view.findViewById(R.id.favouriteIcon)
        Statified.glView = view.findViewById(R.id.visualizer_view)
        Statified.fab.alpha = 0.8f

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

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper.isPlaying = true

        if (Statified.currentSongHelper.isPlaying) {
            Statified.playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
        }

        var path: String? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songId: Long? = null
        var date: Long

        try {

            path = arguments?.getString("path")
            songTitle = arguments?.getString("songTitle")
            songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")!!.toLong()
            date = arguments?.getLong("songDate")!!

            Statified.currentPosition = arguments!!.getInt("songPosition")

            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")!!

            Statified.currentSongHelper.songPath = path
            Statified.currentSongHelper.songTitle = songTitle
            Statified.currentSongHelper.songArtist = songArtist
            Statified.currentSongHelper.songId = songId
            Statified.currentSongHelper.currentPosition = Statified.currentPosition

            songEntities = Entities(
                songId.toLong(),
                songTitle.toString(),
                songArtist.toString(),
                path.toString(),
                Statified.currentPosition,
                date
            )

            val checkFav = DBAsyncTask(activity as Context, songEntities, 1).execute()
            val isFav = checkFav.get()

            if (isFav) {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.favorite_on))

            } else {
                Statified.fab.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.favorite_off))
            }

            Staticated.updateTextViews(
                Statified.currentSongHelper.songTitle as String,
                Statified.currentSongHelper.songArtist as String
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

        var fromFavBottomBar = arguments?.get("favBottomBar") as? String
        if (fromFavBottomBar != null) {
            mediaPlayer = AllSongsFragment.Statified.mediaPlayer
        } else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                mediaPlayer?.setDataSource(myActivity as Context, Uri.parse(path))
                mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer?.start()
        }
        processInformation(mediaPlayer as MediaPlayer)
        if (mediaPlayer!!.isPlaying as Boolean) {
            Statified.playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
        }
        mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }

        clickHandler()

        val visualizationHandler = DbmHandler.Factory.newVisualizerHandler(activity as Context, 0)

        Statified.audioVisualization.linkTo(visualizationHandler)

        var prefsForShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("Feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper.isShuffle = true
            Statified.currentSongHelper.isLoop = false
            Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper.isShuffle = false
            Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("Feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper.isShuffle = false
            Statified.currentSongHelper.isLoop = true
            Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.currentSongHelper.isLoop = false
            Statified.loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
        }
    }

    private fun clickHandler() {

        //play pause event
        Statified.playPauseImageButton.setOnClickListener {
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer!!.pause()
                Statified.currentSongHelper.isPlaying = false
                Statified.playPauseImageButton.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaPlayer!!.start()
                Statified.currentSongHelper.isPlaying = true
                Statified.playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
            }
        }

        Statified.nextImageButton.setOnClickListener {
            Statified.currentSongHelper.isPlaying = true
            Statified.playPauseImageButton.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        }

        Statified.previousImageButton.setOnClickListener {
            Statified.currentSongHelper.isPlaying = true
            if (Statified.currentSongHelper.isLoop) {
                Statified.loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }

        Statified.loopImageButton.setOnClickListener {
            var editorShuffle =activity?.getSharedPreferences(
                MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop =  activity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper.isLoop) {
                Statified.currentSongHelper.isLoop = false
                Statified.loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            } else { Statified.currentSongHelper.isLoop = true
                Statified.currentSongHelper.isShuffle = false
                Statified.loopImageButton.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", true)
                editorLoop?.apply()
            }
        }

        Statified.shuffleImageButton.setOnClickListener {

            var editorShuffle = activity?.getSharedPreferences(
                MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()

            var editorLoop = activity?.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper.isShuffle as Boolean) {
                Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper.isShuffle = false
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper.isShuffle = true
                Statified.currentSongHelper.isLoop = false
                Statified.shuffleImageButton.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("Feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            }
        }

        Statified.fab.setOnClickListener {
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

                    Statified.fab.setImageResource(R.drawable.favorite_on)
                } else {
                    Toast.makeText(
                        context,
                        "Some error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Statified.fab.setImageResource(R.drawable.favorite_off)
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

                    Statified.fab.setImageResource(R.drawable.favorite_off)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)

    }

    override fun onDestroy() {
        super.onDestroy()
        Statified.audioVisualization.release()
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

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }

            override fun onSensorChanged(p0: SensorEvent?) {
                val x = p0!!.values[0]
                val y = p0.values[1]
                val z = p0.values[2]
                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta
                if (mAcceleration > 12) {
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }
        }
    }


}
