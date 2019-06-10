<?php
if (!session_id()) session_start();
error_reporting(0);
if(isset($_SESSION['count'])){
if(!isset($_POST['input'])||!is_numeric($_POST['input'])||intval($_POST['input'])!=10000019){
	session_destroy();
	echo '
	<script language="javascript">  
	alert("must input some big number ~");  
	window.history.back(-1);  </script>';
	}
}
if(preg_match("/[a-zA-Z]+/",$_POST['input'])||preg_match("/[a-zA-Z]+/",$_POST['ans'])){
	echo '
	<script language="javascript">  
	alert("No alphabet please");  
	window.history.back(-1);  </script>';
}
if(!isset($_SESSION['count']))
$_SESSION['count']=0;
if(isset($_SESSION['ans']) && isset($_POST['ans'])){
	if(($_SESSION['ans'])+intval($_POST['input'])!=$_POST['ans']){
		session_destroy();
		echo '
		<script language="javascript">  
		alert("wrong result you know it !");  
		window.history.back(-1);  </script>';
	}
	else{
		if(intval(time())-$_SESSION['time']<2){
			session_destroy();
			echo "time slow:".$_SESSION['time'];
			echo '
			<script language="javascript">  
			alert("slow down !");  
			window.history.back(-1); </script> ';
		}
		if(intval(time())-$_SESSION['time']>3){
			session_destroy();
			echo '
			<script language="javascript">  
			alert("You are a bit of slow");  
			window.history.back(-1); </script> ';
		}
		echo '
		<script language="javascript">  
		alert("right answer");  
	     </script> ';
		$_SESSION['count']++;
	}
}



if($_SESSION['count']>=5){
	session_destroy();
	echo "got flag.....................";
	die();
}
$num1=rand(0,10000000);
$num2=rand(0,10000000);
$num3=rand(0,10000000);
$num4=rand(0,10000000);
$num5=rand(0,10000000);
$num6=rand(0,10000000);
$num7=rand(0,10000000);
$num8=rand(0,10000000);
$num9=rand(0,10000000);
$mark=rand(0,3);

switch($mark){
case 0:
	$_SESSION['ans']=$num1+$num2*$num3+$num4-$num5+$num6*$num7-$num8*$num9;
	break;
case 1:
	$_SESSION['ans']=$num1-$num2+$num3-$num4+$num5+$num6-$num7+$num8-$num9;
	break;
case 2:
	$_SESSION['ans']=$num1*$num2-$num3+$num4+$num5*$num6+$num7-$num8*$num9;
	break;
case 3:
	$_SESSION['ans']=$num1+$num2+$num3*$num4-$num5-$num6+$num7*$num8+$num9;
	break;
}
$_SESSION['time']=intval(time());
echo "time:".$_SESSION['time'];

?>

<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="author" content="Kodinger">
	<meta name="viewport" content="width=device-width,initial-scale=1">
	<title>do some calculation</title>
	<!--think about info leaking please-->
</head>
<body class="my-login-page">
    <div class="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom box-shadow">
      <h5 class="my-0 mr-md-auto font-weight-normal">who can be the master of calculation</h5>
    </div>
	<section class="h h-100">
		<div class="container h-100">
			<h1 class="articles">Try your best</h1>

			<div id="article_list" class="row justify-content-md-center">
                        
                        <a href="#" class="card fat col-md-5 article-card">
			    <div class="card-body">
			     <b class="text-info article">tip:</b>the blank must be filled, and big number should't be a problem
			    </div>
		        </a>
                        
                        <a href="#" class="card fat col-md-5 article-card">
			    <div class="card-body">
			     <b class="text-info article">tip:</b>Old and easy, still this question is tricky
			    </div>
		        </a>
<p  align="right">Now calculating correctly for<?php echo $_SESSION['count'];?>times</p>
		        
                    <form action="" method="post">

					 <input type="text" name="input"><div  style="display:inline;">+</div>
					<?php
					$sentence="";

					switch($mark){
					case 0:
						$sentence="$num1+$num2*$num3+$num4-$num5+$num6*$num7-$num8*$num9=";
						break;
					case 1:
						$sentence="$num1-$num2+$num3-$num4+$num5+$num6-$num7+$num8-$num9=";
						break;
					case 2:
						$sentence="$num1*$num2-$num3+$num4+$num5*$num6+$num7-$num8*$num9=";
						break;
					case 3:
						$sentence="$num1+$num2+$num3*$num4-$num5-$num6+$num7*$num8+$num9=";
						break;
					}
					for($i=0;$i<strlen($sentence);$i++){
						echo "<div style=\"display:inline;\">".$sentence[$i]."</div>";
					}
					?>

					<input type="text" name="ans">
					<input type="submit" value="check">
					</form>    
			</div>
			
		</div>
	</section>
<!--the big number is the fist prime after 1000000 -->
<!--and use your head, my head to solve it out !-->

</body>
</html>





