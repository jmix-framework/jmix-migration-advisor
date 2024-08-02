<!DOCTYPE html>
<html>
<style>
    table, th, td {
        border:1px solid black;
    }
    #total {
        text-align:right;
    }
</style>
<body>

    <h1>General</h1>
    <div>
        <ul>
            <li>Project: ${projectName}</li>
            <li>Entities: ${entitiesAmount}</li>
            <li>Screens: ${screensTotalAmount}</li>
        </ul>
    </div>

    <h2>App Components</h2>
    <div>
        <table style="width:100%">
          <thead>
            <tr>
               <th>Name</th>
               <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <#list appComponents as appComponent>
               <tr>
                   <td>${appComponent.name}</td>
                   <td>${appComponent.notes}</td>
               </tr>
            </#list>
          </tbody>
        </table>
    </div>

    <h1>Estimations</h1>
    <div>
        <table style="width:100%">
          <thead>
            <tr>
               <th>Category</th>
               <th>Estimation (h)</th>
            </tr>
          </thead>
          <tbody>
              <#list estimationItems as estimationItem>
                 <tr>
                     <td>${estimationItem.category}</td>
                     <td>${estimationItem.estimation}</td>
                 </tr>
              </#list>
            </tbody>
          <tfoot>
            <tr>
              <th id="total">Total:</th>
              <td>${totalEstimation}</td>
            </tr>
          </tfoot>
        </table>
    </div>

    <h1>Details & Notes</h1>
    <h2>Screens Complexity</h2>
    <div>
        <table style="width:100%">
          <thead>
            <tr>
               <th>Group</th>
               <th>Amount</th>
               <th>Cost (h)</th>
               <th>Total (h)</th>
            </tr>
          </thead>
          <tbody>
            <#list screenComplexityGroups as group>
               <tr>
                   <td>${group.name}</td>
                   <td>${group.amount}</td>
                   <td>${group.cost}</td>
                   <td>${group.total}</td>
               </tr>
            </#list>
          </tbody>
          <tfoot>
            <tr>
              <th id="total">Total amount :</th>
              <td>${screensTotalAmount}</td>
              <th id="total">Total cost :</th>
              <td>${screensTotalHours}</td>
            </tr>
          </tfoot>
        </table>
    </div>

    <h2>UI Components Notes</h2>
    <div>
        <table style="width:100%">
          <thead>
            <tr>
               <th>Component</th>
               <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <#list uiComponentIssues as issue>
                <tr>
                    <td>${issue.name}</td>
                    <td>${issue.notes}</td>
                </tr>
            </#list>
          </tbody>
        </table>
    </div>

    <h2>Misc</h2>
    <div>
        <table style="width:100%">
          <thead>
            <tr>
               <th>Item</th>
               <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <#list miscNotes as row>
                <tr>
                    <td>${row.name}</td>
                    <td>${row.notes}</td>
                </tr>
            </#list>
          </tbody>
        </table>
    </div>
</body>
</html>