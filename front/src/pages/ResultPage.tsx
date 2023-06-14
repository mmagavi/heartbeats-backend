import {ResultPage_AriaLabel} from "../accessibility/Aria";

/**
 * Props for the result page
 * playlistID: id of playlist to display on results page
 * reset: function to reset playlist id & go back to questionnaire
 */
interface ResultPageProps {
  playlistID: string | undefined; // should be passed in from HomePage
  reset: () => void;
}

/**
 * makes it so that the link is in the user's clipboard. For share button
 * @param link - to be copied into clipboard
 */
function copyLink(link: string) {
  navigator.clipboard.writeText(link);
  alert("Link Copied!");
}

/**
 * ResultPage component: displays result playlist and buttons to share,
 * open spotify, or reset and generate a new playlist
 * @param props - ResultPageProps described above :)
 * @constructor
 */
export default function ResultPage(props: ResultPageProps) {
  console.log(props.playlistID);

  // link to playlist for sharing
  const playlistLink = "https://open.spotify.com/playlist/" + props.playlistID;
  // link for embedded player
  const embedLink =
    "https://open.spotify.com/embed/playlist/" +
    props.playlistID +
    "?utm_source=generator&theme=0";

  const scriptSrc = "https://open.spotify.com/embed-playlist/iframe-api/v1";

  const iframe = (
    <div className="iframeResult">
      {/* taken from Spotify Dev front page */}
      <iframe
    title="Your HeartBeats Result Playlist"
    src={embedLink}
    width={"100%"}
    height={"100%"}
    // allowTransparency={true}
    allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
    loading="lazy"
    />
    </div>
  );

  return (
    <div className="resultsMainBody" aria-label={ResultPage_AriaLabel}>
      <script src={scriptSrc} async/>{" "}
      {/* ^^^ needed for iframe to run*/}
      {iframe}
      {/* Description */}
      <div className="iframeResult" role="playlist">
        <div className="iframeTitle"> Share your heartBeat!</div>
        <p>Our playlist, curated for you with heart</p>
        <p>
          {/* share button */}
          <button
            className="refreshButton"
            role="button"
            onClick={(_) => copyLink(playlistLink)}
          >
            🔗 Link
          </button>
          {/* open in spotify button */}
          <a href={playlistLink} target="_blank">
            <button className="refreshButton" role="button">
              🎧 Open in Spotify
            </button>
          </a>
          <button
            className="refreshButton"
            role="button"
            onClick={(_) => props.reset()}
          >
            💓 Get a New Beat!
          </button>

          {/* refresh quiz button */}
        </p>
      </div>
    </div>
  );
}
