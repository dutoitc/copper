package ch.mno.copper.daemon;

import ch.mno.copper.collect.StoryTask;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoryTaskRunnableTest {

    @Test
    void testAll() {
        StringBuilder sb = new StringBuilder();
        StoryTask task = Mockito.mock(StoryTask.class);
        Mockito.when(task.getRunnable()).thenReturn(()->sb.append("Ran"));
        new StoryTaskRunnable(task).run();
        Mockito.verify(task).markAsRun();
        assertEquals("Ran", sb.toString());
    }

    @Test
    void testNPE() {
        StoryTask task = Mockito.mock(StoryTask.class);
        Mockito.when(task.getRunnable()).thenReturn(()-> {
            throw new NullPointerException("Exception for test");
        });
        new StoryTaskRunnable(task).run();
        Mockito.verify(task).markAsRun();
    }

    @Test
    void testRE() {
        StoryTask task = Mockito.mock(StoryTask.class);
        Mockito.when(task.getRunnable()).thenReturn(()-> {
            RuntimeException e = new RuntimeException("Exception for test");
            throw new RuntimeException("ExceptionTest2", e);
        });
        new StoryTaskRunnable(task).run();
        Mockito.verify(task).markAsRun();
    }

}
