<?php

error_reporting(E_ALL ^ E_NOTICE);

$conn =new mysqli("localhost","root","","techSchedulerDB");

if($conn->connect_error){
	die("Connection failed ".$conn->connect_error);
}


 $username = $_POST['username'];
 $password = $_POST['password'];
 $email=$_POST['email'];
 $phone=$_POST['phone'];
 $firstName=$_POST['firstName'];
 $surname=$_POST['surname'];

$sql = "INSERT INTO technician (username,password,email,phone,firstName,surname) VALUES ('$username','$password','$email','$phone','$firstName','$surname') ";

$response = array();


if($conn->query($sql)===TRUE){
    $response["success"] = 1;
}else{
    $response["success"] = 0;

}

echo json_encode($response);
//以json的形式返回给客户端

$conn->close();
?>
