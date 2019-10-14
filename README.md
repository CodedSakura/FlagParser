# Java Flag Parser
Parses more than just flags.  
## Usage
`FlagParser fp = new FlagParser(String input)`  
`input` - input string, should look similar to a linux-like command argument  
`fp.hasArgs(), fp.getArgs()` - returns `String[]` containing all arguments from `input` (in order)  
`fp.hasOps(), fp.getOps()` - returns `String[]` containing all options (`-aB --opt`) (ordered)  
`fp.hasPairs(), fp.getPairs()` - returns `Map<String, String>` containing all option-value pairs (`--opt=val`)  
## Development
No dev libs are required, but to run unit tests, you need JUnit5