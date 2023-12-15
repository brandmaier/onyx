# Function to read content from preamble.txt
read_preamble <- function() {
  preamble_content <- readLines("apache20-preamble.txt")
  return(preamble_content)
}

# Function to process each Java file
process_java_file <- function(file_path, preamble_content) {
  # Read the content of the Java file
  java_content <- readLines(file_path)
  
  # Paste the content of preamble.txt at the beginning of the Java file
  modified_content <- c(preamble_content, java_content)
  
  # Overwrite the Java file with the modified content
  writeLines(modified_content, con = file_path)
}

# Function to process all Java files in a directory
process_java_files <- function(directory_path) {
  # Get the content of preamble.txt
  preamble_content <- read_preamble()
  
  # List all Java files in the directory recursively
  java_files <- list.files(directory_path, pattern = "\\.java$", recursive = TRUE, full.names = TRUE)
  
  # Process each Java file
  for (file_path in java_files) {
    process_java_file(file_path, preamble_content)
  }
}

# Specify the directory to recurse through
directory_path <- "src"

# Call the function to process Java files in the specified directory
process_java_files(directory_path)