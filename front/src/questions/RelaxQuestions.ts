export const RelaxQuestions = [
  {
    // a multiplechoice question should look like this
    question:
      "Would you like to start quickly or start wind up?",
    id: 1,
    choices: [
      {
        text: "Gotta go fast!",
        img: "https://discovery.sndimg.com/content/dam/images/discovery/fullset/2020/4/2/nightsky2_getty.jpg.rend.hgtvcom.406.305.suffix/1585862428885.jpeg",
        val: "quick",
      },
      {
        text: "Give me a warmup!",
        img: "https://s3-us-west-1.amazonaws.com/contentlab.studiod/getty/31a4debc7443411195df509e38a5f9a3.jpg",
        val: "full",
      },
    ],
  },
  {
    question: "Would you like to wind down quickly or gradually?",
    id: 2,
    choices: [
      {
        text: "Less Winddown",
        img: "https://s3-us-west-1.amazonaws.com/contentlab.studiod/getty/31a4debc7443411195df509e38a5f9a3.jpg",
        val: "quick",
      },
      {
        text: "More Winddown",
        img: "https://discovery.sndimg.com/content/dam/images/discovery/fullset/2020/4/2/nightsky2_getty.jpg.rend.hgtvcom.406.305.suffix/1585862428885.jpeg",
        val: "slow",
      },
    ],
  },
  {
    // a slider question should look like this
    question: "What is your resting heart rate?",
    id: 3,
  },
  {
    // a slider question should look like this
    question: "What is your age?",
    id: 4,
  },
  {
    // a slider question should look like this
    question: "How long would you like your winddown to be?",
    id: 5,
  }
];
