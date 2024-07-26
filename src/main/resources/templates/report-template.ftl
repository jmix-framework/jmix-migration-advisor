<!DOCTYPE html>
<html>
<style>
table, th, td {
  border:1px solid black;
}
</style>
<body>

<table style="width:100%">
  <tr>
     <th>Group</th>
     <th>Amount</th>
     <th>Cost</th>
     <th>Total</th>
  </tr>
  <#list groups as group>
     <tr>
         <td>${group.name}</td>
         <td>${group.screens}</td>
         <td>${group.cost}</td>
         <td>${group.total}</td>
     </tr>
  </#list>
</table>


</body>
</html>