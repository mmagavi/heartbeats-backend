export type questionsType = radioQuestion | sliderQuestion;
type radioQuestion = {
  question: string;
  id: number;
  choices: {
    text: string;
    img: string;
    val: string;
  }[];
};

type sliderQuestion = {
  question: string;
  id: number;
};
