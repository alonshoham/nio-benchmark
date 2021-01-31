package nio;

public class Util {
    public static String poolType = "fixed";
    public static int poolSize = 4;

    public static void parseArgs(String argv[]){
        if(argv != null){
            for (String arg: argv){
                String[] parsed = arg.split("=");
                if(parsed != null && parsed.length == 2) {
                    String key = parsed[0];
                    String value = parsed[1];
                    if (key.equals("poolSize"))
                        poolSize = Integer.valueOf(value);
                    if (key.equals("poolType"))
                        poolType = value;
                }

            }
        }
    }
}
