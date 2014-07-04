<?php
$target_path = "../data/uploads";
$target_path1 = $target_path . "/";

/* Add the original filename to our target path.
Result is "uploads/observ_no/filename.extension" */
$dir_pattern = "/(?<=test_)\d+/";
preg_match($dir_pattern, basename($_FILES['uploaded_file']['name']), $match);
$target_directory = $target_path1 . $match[0];
$target = $target_directory . "/" . basename( $_FILES['uploaded_file']['name']);
$is_first = FALSE; /*Is the first file to upload to target dir*/
if(is_dir($target_directory)){
    $is_first = FALSE;
}else{
    $is_first = TRUE;
    $scanned_dirs = array_diff(glob($target_path . "/*", GLOB_ONLYDIR), array('..','.'));
    $num_dirs =	count($scanned_dirs);
    $checked_dir = $scanned_dirs[$num_dirs-2];
    $scanned_files = array_diff(scandir($checked_dir), array('..','.'));
    $num_files = count($scanned_files);
    $scanned_txts = array_diff(glob($checked_dir . "/*.txt"), array('..','.'));
    $num_txts = count($scanned_txts);
    $scanned_wavs = array_diff(glob($checked_dir . "/*.wav"), array('..','.'));
    $num_wavs =	count($scanned_wavs);
    if($num_txts != 2){
        foreach($scanned_files as $file){
            unlink($checked_dir . "/" . $file);
        }
	rmdir($checked_dir);
    }elseif($num_wavs == 1){
        foreach($scanned_wavs as $file){
            unlink($file);
        }
    }elseif($num_wavs == 2){
        $wav_size1 = filesize($scanned_wavs[0]);
        $wav_size2 = filesize($scanned_wavs[1]);
	if(!($wav_size1 > 100 && $wav_size2 > 100)){
	    foreach($scanned_wavs as $file){
                unlink($file);
            }
	}
    }
    
    mkdir($target_directory);
}

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target)){
    echo "The first file ".  basename( $_FILES['uploaded_file']['name']).
    " has been uploaded.";
}else{
    echo "There was an error uploading the file, please try again!";
    echo "filename: " .  basename( $_FILES['uploaded_file']['name']);
    echo "target_path: " .$target_path1;
}

?>

