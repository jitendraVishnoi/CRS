<%--
  This gadget includes another gadgets to render billing form content.

  It allows to use existing credit card or create new card. When creating new card,
  the user can choose an existing address or create new one.

  Required parameters:
    None.

  Optional parameters:
    None.
--%>
<dsp:page>
  <dsp:importbean bean="/atg/store/order/purchase/BillingFormHandler" />
  <dsp:importbean bean="/atg/store/droplet/EnsureCreditCard"/>
  <dsp:importbean bean="/atg/commerce/order/purchase/ShippingGroupDroplet"/>
  <dsp:importbean bean="/atg/commerce/ShoppingCart"/>
  <dsp:importbean bean="/atg/userprofiling/Profile"/>
  <dsp:importbean bean="/atg/store/droplet/ProfileSecurityStatus"/>
  <dsp:importbean bean="/atg/store/droplet/AvailableBillingAddresses"/>
  <dsp:importbean bean="/atg/commerce/order/purchase/ShippingGroupContainerService"/>

  <dsp:getvalueof var="order" vartype="atg.commerce.Order" bean="ShoppingCart.current"/>

  <%-- INITALIZE COMMERCE SHIPPING OBJECTS --%>
  <%--
    Used to initialize ShippingGroups and CommerceItemShippingInfo objects.

    Input Parameters:
      createOneInfoPerUnit
        Creates one CommerceItemShippingInfo for each individual unit contained by a commerce item.
      clear
        Clear both the CommerceItemShippingInfoContainer and the ShippingGroupMapContainer.
      shippingGroupTypes
        Comma separated list of ShippingGroup types used to determine which ShippingGroupInitializer
        components are executed.
      initShippingGroups
        ShippingGroup types will be initialized.
      initBasedOnOrder
        Create a CommerceItemShippingInfo for each ShippingGroupCommerceItemRelationships in the order.

    Open Parameters:
      output
  --%>
  <dsp:droplet name="ShippingGroupDroplet">
    <dsp:param name="createOneInfoPerUnit" value="true"/>
    <dsp:param name="clearShippingInfos" value="true"/>
    <dsp:param name="shippingGroupTypes" value="hardgoodShippingGroup"/>
    <dsp:param name="initShippingGroups" value="true"/>
    <dsp:param name="initBasedOnOrder" value="true"/>
    <dsp:oparam name="output"/>
  </dsp:droplet>

  <%-- Each order should possess one Credit Card payment group. Check this and create group, if needed. --%>
  <dsp:droplet name="EnsureCreditCard">
    <dsp:param name="order" bean="ShoppingCart.current"/>
  </dsp:droplet>

  <div class="atg_store_checkoutOption" id="atg_store_checkoutOptionArea">
	<script type="text/javascript">
function enableCreditCardEntry() {
	document.getElementById("creditCardFormDiv").style.display = 'block';
	clearCheck("paypalRadio");
	atg.store.checkoutNav.billingShippingHeightFix();
}
function disableCreditCardEntry() {
	document.getElementById("creditCardFormDiv").style.display = 'none';
	clearCheck("creditCardRadio");
	atg.store.checkoutNav.billingShippingHeightFix();
}
function clearCheck(radioId) {
	document.getElementById(radioId).checked = false;
}
</script>

<table width="100%">
<tr><td colspan="2">

        <h2 style="color:#999900;font-size:2.2em;margin-bottom:10px;">Payment Method</h2>


</td></tr>
<tr style="background-color: #D8D8BF">
			<td style="padding-top:50px;padding-bottom:50px;padding-left:10px;">
				<input type="radio" id="paypalRadio" onclick="disableCreditCardEntry();" checked/>
				<img src="https://www.paypal.com/en_US/i/logo/PayPal_mark_37x23.gif" style="vertical-align: middle" alt="PayPal">
				<span style="font-size: 11px; font-family: Arial, Verdana;"> The safer, easier way to pay.
				<a href="#" onclick="javascript:window.open('https://www.paypal.com/cgi-bin/webscr?cmd=xpt/popup/OLCWhatIsPayPal-outside');">What is PayPal?</a>
				<dsp:input type="hidden" bean="BillingFormHandler.callback" value="true" />
				</span>
			</td>
			<td>

			<dsp:droplet
				name="/crs/paypal/droplet/PayPalPaymentDroplet">
				<dsp:param name="profile" bean="/atg/userprofiling/Profile" />

				<dsp:oparam name="express">
					<dsp:input id="atg_store_paypal_checkout" type="image" bean="BillingFormHandler.checkoutWithPayPal"
				value="PayPal Checkout" src="https://www.paypal.com/en_US/i/btn/x-click-but6.gif" />
				</dsp:oparam>

				<dsp:oparam name="billing">
					<dsp:input id="atg_store_paypal_checkout" type="image" bean="BillingFormHandler.billingWithPayPalBillingAgreement"
				value="PayPal Checkout" src="https://www.paypal.com/en_US/i/btn/x-click-but6.gif" />
				</dsp:oparam>

				<dsp:oparam name="error">
					Something wrong happened!!
				</dsp:oparam>
			</dsp:droplet>

			</td>
				<br />

</tr>
<tr><td>
			<div style="padding: 0px 0px 0px 10px;">
			<br/>
				-OR-
			<br/>
			<br/>
			</div>
</td></tr>
<tr><td>
			<div style="padding: 10px;">
				<input type="radio" id="creditCardRadio" onclick="enableCreditCardEntry();" />
				<img src="/crsdocroot/images/storefront/credit_card_logos.gif" style="vertical-align: middle" alt="Credit Cards" />
				<span style="font-size: 11px; font-family: Arial, Verdana;"> Pay via Credit Card.</span>
			</div>
</td></tr>
</table>


	<div id="creditCardFormDiv" style="display: none;">

    <dsp:getvalueof var="creditCards" vartype="java.lang.Object" bean="Profile.creditCards"/>

    <c:if test="${!empty creditCards}">

      <%-- Tell the user part of the order total was paid using store credits --%>
      <c:if test="${order.storeCreditsAppliedTotal > 0}">
        <div class="atg_store_appliedStoreCredit">

          <dsp:include page="/global/gadgets/formattedPrice.jsp">
            <dsp:param name="price" value="${order.storeCreditsAppliedTotal }"/>
          </dsp:include>

          <fmt:message key="checkout_billing.storeCreditApplied"/>

        </div>
      </c:if>

      <%-- Tab with a list of saved credit cards. --%>
      <div class="atg_store_existingCreditCards">
        <h3 class="atg_store_usedSavedCardHeader"><fmt:message key="checkout_billing.useSavedCreditCard"/></h3>
        <fieldset class="atg_store_savedCreditCard">
          <dsp:include page="savedCreditCards.jsp" />
        </fieldset>
      </div>
    </c:if>

    <%-- New credit card form --%>
    <div class="atg_store_addCardInfo">
      <fieldset class="atg_store_creditCardForm">
        <h3>
          <fmt:message key="checkout_billing.newCreditCard"/>
        </h3>
        <dsp:include page="creditCardForm.jsp" />
      </fieldset>
    </div>

    <%--
      shippingGroupMap contains address information from the profile and the order we only want to
      check if we have permitted billing addresses if we have any addresses.
    --%>
    <dsp:getvalueof var="shippingGroupMap" vartype="java.lang.Object"
                    bean="ShippingGroupContainerService.shippingGroupMapForDisplay"/>

    <c:if test="${not empty shippingGroupMap}">
      <%--
        Sorts billing addresses so that the default address is first and returns only permitted shipping
        addresses. (E.g Do we ship to this country?)

        Input parameters:
          defaultKey
            The parameter that defines the map key of the default item that should be placed
            in the beginning of the array.
          sortByKeys
            Boolean that specifies whether to sort map entries by keys or not.
          map
            The parameter that defines the map of items to convert to the sorted array.

        Open parameters:
          output
            Rendered for permitted shipping address list, permittedAddresses parameter contains
            the permitted shipping addresses.
          empty
            Rendered if there are no shipping addresses or there are no permitted shipping addresses

        Output parameters:
          permittedAddresses
            Contains the permitted shipping addresses.
      --%>
      <dsp:droplet name="AvailableBillingAddresses">
        <dsp:param name="map" value="${shippingGroupMap}"/>
        <dsp:param name="defaultId" bean="Profile.shippingAddress.repositoryId"/>
        <dsp:param name="sortByKeys" value="true"/>
        <dsp:oparam name="output">
          <dsp:getvalueof var="permittedAddresses" vartype="java.lang.Object" param="permittedAddresses"/>
        </dsp:oparam>
      </dsp:droplet>
    </c:if>

    <div id="atg_store_chooseCardAddress" class="${!empty permittedAddresses?'atg_store_existingAddresses ':''}">

      <c:if test="${not empty permittedAddresses}">
        <div id="atg_store_selectAddress">
          <h4>
            <fmt:message key="checkout_billing.useSavedAddresses"/>
          </h4>
          <fieldset class="atg_store_billingAddresses">

            <%-- Render the possible billing addresses --%>
            <dsp:include page="billingAddresses.jsp">
              <dsp:param name="availableBillingAddresses" value="${permittedAddresses}"/>
            </dsp:include>

            <%-- Continue button --%>
            <div class="atg_store_saveSelectAddress atg_store_formActions">
              <span class="atg_store_basicButton">

                <fmt:message var="continueButtonText" key="common.button.continueText"/>

                <dsp:input type="submit" bean="BillingFormHandler.billingWithSavedAddressAndNewCard"
                           value="${continueButtonText}" alt="${continueButtonText}" id="submit"
                           iclass="atg_store_actionSubmit atg_behavior_disableOnClick"/>
              </span>
              <p>
                <fmt:message key="checkout_billing.usingSavedAddress"/>
              </p>
            </div>
          </fieldset>
        </div>
      </c:if>

      <%-- Display 'Create new Billing Address' input fields --%>
      <div id="atg_store_enterNewBillingAddress">
        <h4>
          <fmt:message key="checkout_billing.newBillingAddress"/>
        </h4>

        <fieldset class="atg_store_newBillingAddress">
          <dsp:include page="billingAddressAdd.jsp"/>
        </fieldset>

        <fmt:message var="reviewOrderButtonText" key="common.button.continueText"/>

        <%-- Continue with new address button. --%>
        <div class="atg_store_saveNewBillingAddress atg_store_formActions">
          <span class="atg_store_basicButton">

            <dsp:input type="submit" bean="BillingFormHandler.billingWithNewAddressAndNewCard"
                       value="${reviewOrderButtonText}" alt="${reviewOrderButtonText}" id="submit"
                       iclass="atg_store_actionSubmit atg_behavior_disableOnClick"/>
          </span>
          <p>
            <fmt:message key="checkout_billing.usingNewAddress"/>
          </p>
        </div>
      </div>
    </div>

    <%-- Hidden URL-related fields. --%>
    <fieldset class="atg_store_checkoutContinue">
      <div class="atg_store_formFooter">
        <%--
          Check if user is anonymous or the session has expired. If so set the sessionExpirationURL

          Open parameters:
            anonymous
              User is not logged in.
            default
              User has been recognized.
        --%>
        <dsp:droplet name="ProfileSecurityStatus">
          <dsp:oparam name="anonymous">
            <dsp:input type="hidden" bean="BillingFormHandler.sessionExpirationURL"
                       value="${pageContext.request.contextPath}/home"/>
          </dsp:oparam>
          <dsp:oparam name="default">
            <dsp:input type="hidden" bean="BillingFormHandler.sessionExpirationURL"
                       value="${pageContext.request.contextPath}/checkout/login.jsp"/>
          </dsp:oparam>
        </dsp:droplet>

        <%-- Success/error URLs. --%>
        <dsp:input type="hidden" bean="BillingFormHandler.moveToConfirmSuccessURL"
                   value="confirm.jsp"/>
        <dsp:input type="hidden" bean="BillingFormHandler.moveToConfirmErrorURL"
                   value="billing.jsp?preFillValues=true"/>
      </div>
    </fieldset>
  </div>
  </div>
</dsp:page>
