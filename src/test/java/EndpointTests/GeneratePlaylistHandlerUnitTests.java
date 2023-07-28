//package EndpointTests;
//
//import ExceptionClasses.EndpointParamExceptions.NotBooleanException;
//import ExceptionClasses.InvalidInputExceptions.*;
//import Server.GeneratePlaylistHandler;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//public class GeneratePlaylistHandlerUnitTests {
//
//
//    // verifyGenre tests
//    @Test
//    public void passingInvalidGenreThrowsException(){
//
//        Assertions.assertThrows(InvalidGenreException.class,
//                () -> GeneratePlaylistHandler.verifyGenre("Invalid_Genre"));
//    }
//
//    @Test
//    public void passingValidGenreDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyGenre("hip-hop"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyGenre("BriTiSH"));
//    }
//
//    // processBoolean tests
//    @Test
//    public void passingInvalidBooleanThrowsException(){
//        Assertions.assertThrows(NotBooleanException.class,
//                () -> GeneratePlaylistHandler.processBoolean("Invalid_Boolean"));
//    }
//
//    @Test
//    public void passingFalseDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.processBoolean("false"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.processBoolean("FaLSE"));
//    }
//
//    @Test
//    public void passingTrueDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.processBoolean("true"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.processBoolean("TRue"));
//    }
//
//    // verifyPlaylistType tests
//    @Test
//    public void passingInvalidPlaylistTypeThrowsException(){
//        Assertions.assertThrows(InvalidPlaylistTypeException.class,
//                () -> GeneratePlaylistHandler.verifyPlaylistType("Invalid_Playlist_Type"));
//    }
//
//    @Test
//    public void passingValidPlaylistTypeDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyPlaylistType("winding_down"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyPlaylistType("wiNDing_DOwn"));
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyPlaylistType("working_out"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyPlaylistType("wORKing_OUT"));
//    }
//
//    // verifyAge tests
//
//    @Test
//    public void passingInvalidAgeThrowsException(){
//        Assertions.assertThrows(InvalidAgeException.class,
//                () -> GeneratePlaylistHandler.verifyAge(-55));
//
//        Assertions.assertThrows(InvalidAgeException.class,
//                () -> GeneratePlaylistHandler.verifyAge(101));
//    }
//
//    @Test
//    public void passingValidAgeDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyAge(13));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyAge(100));
//    }
//
//    // verifyCoolDown tests
//    @Test
//    public void passingInvalidCoolDownThrowsException(){
//        Assertions.assertThrows(InvalidCoolDownException.class,
//                () -> GeneratePlaylistHandler.verifyCoolDown("Invalid_Cool_Down"));
//    }
//
//    @Test
//    public void passingValidCoolDownDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyCoolDown("long_cool_down"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyCoolDown("long_COOL_down"));
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyCoolDown("short_cool_down"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyCoolDown("SHOrt_cool_dOWN"));
//    }
//
//    // verifyWorkoutLength tests
//
//    @Test
//    public void passingInvalidWorkoutLengthThrowsException(){
//        Assertions.assertThrows(InvalidWorkoutLength.class,
//                () -> GeneratePlaylistHandler.verifyWorkoutLength(14));
//
//        Assertions.assertThrows(InvalidWorkoutLength.class,
//                () -> GeneratePlaylistHandler.verifyWorkoutLength(181));
//    }
//
//    @Test
//    public void passingValidWorkoutLengthDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWorkoutLength(15));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWorkoutLength(180));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWorkoutLength(69));
//
//    }
//
//    @Test
//    public void passingInvalidWarmupThrowsException(){
//        Assertions.assertThrows(InvalidWarmupException.class,
//                () -> GeneratePlaylistHandler.verifyWarmup("Invalid"));
//    }
//
//    @Test
//    public void passingValidWarmupDoesNotThrowException(){
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWarmup("start_quickly"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWarmup("start_QUICKLY"));
//
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWarmup("start_with_warmup"));
//        Assertions.assertDoesNotThrow(() -> GeneratePlaylistHandler.verifyWarmup("STArt_WITH_warmUP"));
//    }
//
//
//
//}