package fr.avianey.mojo.androidgendrawable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@RunWith(Parameterized.class)
public class NinePatchMatchingTest {

	private static final String PATH_IN  = "./target/test-classes/" + NinePatchMatchingTest.class.getSimpleName() + "/";

    private final String fileName;
    private final String resourceName;
    private final Map<Qualifier.Type, String> typedQualifiers;
    private final boolean resultExpected;
    private final String nameExpected;

    public NinePatchMatchingTest(String fileName, String resourceName, 
            String qualifiedString, boolean resultExpected, String nameExpected) {
         this.fileName = fileName;
         this.resourceName = resourceName;
         this.typedQualifiers = Qualifier.fromQualifiedString(qualifiedString);
         this.resultExpected = resultExpected;
         this.nameExpected = nameExpected;
    }
    
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {"9patch-no-regexp.json", "matching_name",
                            null,
                            true,
                            "matching_name"
                        },
                        {"9patch-no-regexp.json", "non_matching_name",
                            null,
                            false,
                            null
                        },
                        {"9patch-simple-regexp.json", "matching_name",
                            null,
                            true,
                            "matching_.*"
                        },
                        {"9patch-simple-regexp.json", "non_matching_name",
                            null,
                            false,
                            null
                        },
                        {"9patch-multiple-regexp-1.json", "matching_name",
                            "long",
                            false,
                            null
                        },
                        {"9patch-multiple-regexp-1.json", "matching_name",
                            "land",
                            true,
                            "matching_.*"
                        },
                        {"9patch-multiple-regexp-2.json", "matching_name",
                            "w700dp-land-fr-xlarge",
                            true,
                            "ma.*"
                            
                        },
                        {"9patch-multiple-regexp-2.json", "matching_name",
                            "w700dp-land-fr",
                            true,
                            "matching_.*"
                        },
                        {"9patch-multiple-regexp-2.json", "matching_name",
                            "w700dp-port-h400dp",
                            false,
                            null
                        },
                        {"9patch-multiple-regexp-2.json", "matching_name",
                            "w700dp-land-h400dp",
                            false,
                            null
                        },
                        {"9patch-multiple-regexp-2.json", "matching_name",
                            "xlarge",
                            false,
                            null
                        }
                });
    }
    
    @Test
    public void fromJson() throws URISyntaxException, JsonIOException, JsonSyntaxException, IOException {
        try (final Reader reader = new InputStreamReader(new FileInputStream(PATH_IN + fileName))) {
            Type t = new TypeToken<Set<NinePatch>>() {}.getType();
            Set<NinePatch> ninePatchSet = new GsonBuilder().create().fromJson(reader, t);
            NinePatchMap ninePatchMap = NinePatch.init(ninePatchSet);
            
            QualifiedResource mockedResource = Mockito.mock(QualifiedResource.class);
            Mockito.when(mockedResource.getName()).thenReturn(resourceName);
            Mockito.when(mockedResource.getTypedQualifiers()).thenReturn(typedQualifiers);
            
            NinePatch ninePatch = ninePatchMap.getBestMatch(mockedResource);
            Assert.assertTrue(resultExpected ^ (ninePatch == null));
            if (resultExpected && ninePatch != null) {
            	Assert.assertEquals(nameExpected, Reflect.on(ninePatch).get("name"));
            }
        }
    }
    
}
