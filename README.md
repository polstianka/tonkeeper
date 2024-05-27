# tonkeeper

# About this submission

- There are **all three** features implemented 
- It really **is** native. It means that this solution does not use third-party SDK in order to form
a blockchain message for swap/staking features. This feature makes the solution less error-prone and 
better performance-wise (hence, faster). 
- It is almost pixel-perfect and aligns to the designs provided by the contest organizers.

## Staking

Supports all 3 implementations of staking pools:
- Tonstakers
- TON Whales
- TON Nominators

### Stake screen

To navigate to this screen, just click on `Stake` on the main screen.

You can pick the preferred one from `Stake` screen. When picking, you've got the pool with `Max APY`
highlighted which helps making the right choice. By default, the pool with max APY is picked.

When pool is picked, you've got to enter the amount of `TON` you wish to stake. Form here validates 
the input, and does not allow user to enter neither the amount they do not currently have nor amount
less than minimal staking field stated in pool info. 

For users who are in hurry, there is a `MAX` button, that sets the amount user wishes to stake equal
to the `TON` balance user currently have.

Users who are new to staking can get an additional info provided by the `info` button on the top 
left corner. 

There is a `Continue` button. It is only active when user input is valid. When clicked, navigates 
user to `Confirm stake screen`. 

### Stake options screen

On this screen, there are all three pool implementations present (at least this is correct for the 
mainnet). The pool implementation that user has picked is highlighted with active radiobutton. When 
user clicks one of those implementations, there are two possible outcomes:
1. There is only one pool for given implementation (true for Tonstakers). In this case, user is 
being navigated to this one `Staking pool details screen`.
2. There are more than one pool for the given implementation. In this case, user is being navigated 
to the `Staking pools list screen`

The pool implementation that has the highest `APY` is highlighted with `Max APY` sign. 

There are two navigation bar buttons:
1. Chevron in top left corner. Navigates back to the `Stake screen` 
2. Cross in top right corner. Closes the flow and navigates user to the main screen.

### Staking pools list screen

It looks pretty much the same as `Stake options screen`. If user has the pool from given 
implementation picked, this pool is highlighted with active radiobutton. Pool with the highest `APY`
is highlighted with `Max APY` sign.

When clicked on any pool, user is being navigated to the `Staking pool details screen`.

There are two navigation bar buttons:
1. Chevron in top left corner. Navigates back to `Stake options screen`.
2. Cross in top right corner. Closes the flow and navigates user to the main screen.

### Staking pool details screen

Has all the information in regards to the given pool. 

Header displays the pool name. 

Pool `APY` and `Minimal deposit` are displayed too. If `APY` is maximal, shows `Max APY` sign.

Also shows the links for the given pool. There are always present:
1. Link to the pool provider website (e.g. `tonwhales.com`)
2. Link to the `tonviewer` website where user can check the latest transactions for the pool.

Other links are optional and only appear when provided. When link is clicked, user is being 
redirected to the browser. 

If pool supports liquid staking, shows details for the corresponding token. 

Has `Continue` button. When this button is clicked, navigates user to the `Stake screen`. Updates state
of the `Stake screen`, switching picked pool. 

There are two navigation bar buttons:
1. Chevron in top left corner. Navigates back to either `Staking pools list screen` or `Stake
   options screen`, depending on where user came from.
2. Cross in top right corner. Closes the flow and navigates user to the main screen.

### Confirm stake screen

Displays the pool icon.

Displays the amount of `TON` user wishes to stake. The amount is being provided from `Stake screen`.
This means it is validated.

Displays the equivalent in currency picked by user. 

Displays the wallet name.

Displays the recipient pool name.

Displays the fee in both `TON` and user-preferred fiat currency.

Has a slider. When slider is slided, hides the slider and shows the loader animation on it's place.
When request is handled, there are two possible outcomes.
1. Request is successfully published to the blockchain. In this case, shows green checkmark with 
`Done` text and finishes the flow after a second. 
2. Request is not published to the blockchain. In this case, shows red cross with `Error` text.

When flow is finished, user is being navigated to the history screen, where their transaction is 
being pending.

Navigation bar has 2 buttons:
1. Chevron on the top left corner. Navigates user back to `Stake screen`.
2. Cross on the top right corner. Closes the flow and navigates user to the main screen.

### Main screen

When user has some `TON` staked, there will be changes on the main screen. 
1. If user has some liquid staking jetton (e.g. `tsTON`), instead of displaying it as a jetton, app
will display it as a staked balance. 
2. If user has simple staking, it will also be displayed right under the jetton balances. 

There are some catches with balances of those items:
1. If staking has pending deposit/withdrawal, it won't be displayed in the staking balance since this
balance is not mobile yet. However, it will be summed up for overall balance. So after staking 50 TON,
your overall balance won't change much. However, your staked asset will be at 0 until the next staking
cycle starts. 
2. When pending deposit/withdrawal are present, it'll be stated with a corresponding message on a 
given item.

When any of those items are clicked, user is being navigated to the `Staking balance screen`.

### Staking balance screen

It has pretty much all the information from `Staking pool details screen`. But is not limited to it.
This screen also shows the balance of staked asset in both `TON` and fiat. 

When user have pending withdrawal, it shows corresponding message.

Same is true for pending deposit. 

If pool has liquid staking jetton, it's balance will be shown on this screen. User can click this button.
This will navigate user to the jetton balance screen. 

There also are `Stake` and `Unstake` buttons.
`Stake` leads to the `Stake screen`. On stake screen, picked poll will be the same that you navigated 
from 

`Unstake` button leads to `Unstake screen`. 

### Unstake screen

This screen is pretty much the same as `Stake screen`. However, it does not allow user to pick the 
pool to unstake, since it remembers it from `Staking balance screen`. 

Also displays the time user have to wait till the validation cycle is over.

Has `Continue` button. When button is clicked, leads to `Confirm unstake screen`.

### Confirm unstake screen

Works the same way as `Stake screen` but the operation that is done when confirmed is quite the 
opposite.

## Swap

### Swap screen

### Choose token screen

### Swap settings screen

### Confirm swap screen

## Buy/sell

### Buy screen 

### Sell screen

### Pick country screen

### Pick currency screen

### Pick operator screen

### Billing web-view
